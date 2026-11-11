# ADR-001: order-service gọi catalog-service đồng bộ (REST) có timeout và cầu dao, không dùng bất đồng bộ

- **Trạng thái:** Đã chấp nhận
- **Ngày:** 2026-11-11
- **Người quyết định:** Huỳnh Khôi Nguyên
- **Liên quan:** CT230 Lab 3 (TH3-2, TH3-3, TH3-4, TH3-5, TH3-6, TH3-7); ADR sau này nếu thêm hàng đợi cho inventory

## 1. Bối cảnh
BookStore đã tách khỏi nguyên khối thành các dịch vụ: `order-service` (8082), `catalog-service` (8081) giữ giá, inventory-service` (8083). Khi nhận đơn, `order-service` phải biết **giá hiện tại** của từng sách để tính tổng đơn và trả ngay `201` kèm tổng tiền cho ứng dụng di động.

Sự cố tuần trước: catalog chậm 10 giây làm toàn bộ luồng đặt hàng treo. Nguyên nhân là lời gọi không có timeout đọc, nên mỗi yêu cầu giữ một luồng Tomcat 10 giây và lỗi lan truyền sang order-service. Yêu cầu nghiệp vụ: lỗi của catalog phải **không lan truyền**, và gửi lại yêu cầu đặt hàng khi mạng chập chờn không được tạo đơn trùng.

## 2. Các phương án đã xem xét
| Phương án | Ưu điểm | Nhược điểm |
|---|---|---|
| A. Gọi đồng bộ REST, giữ nguyên như cũ (không timeout, không cầu dao) | Đơn giản nhất | Chính là nguyên nhân sự cố: catalog chậm kéo cả order-service treo luồng |
| B. Gọi đồng bộ REST + timeout kết nối/đọc + cầu dao (Resilience4j) + trả 503 nhanh | Giữ được phản hồi tức thì có giá; lỗi bị chặn ở biên, trả lỗi nhanh; ít thành phần mới | Khi catalog hỏng, đặt hàng bị từ chối (đánh đổi tính sẵn sàng); vẫn phụ thuộc thời gian thực vào catalog |
| C. Bất đồng bộ qua hàng đợi (Kafka/RabbitMQ): nhận đơn trước, tính giá sau | Tách rời hoàn toàn về thời gian, chịu được catalog ngừng lâu | Không trả được tổng tiền ngay; thêm hạ tầng và độ phức tạp (nhất quán cuối cùng, xử lý lỗi, trùng sự kiện); quá nặng cho một lần tra giá |
| D. Gọi đồng bộ + thử lại (retry) tự động, không cầu dao | Che được lỗi thoáng qua | Khi catalog chậm, retry nhân thêm tải lên dịch vụ đang yếu, làm tình trạng xấu đi |

## 3. Quyết định
Chọn **phương án B**: `order-service` gọi `catalog-service` đồng bộ bằng `RestClient`, kèm:

- **Timeout kết nối 1 s** (đặt trên `HttpClient` của JDK) và **timeout đọc 2 s** (đặt trên `JdkClientHttpRequestFactory`), cấu hình từ `bookstore.catalog.*`.
- **Cầu dao "catalog"** (Resilience4j): cửa sổ trượt 10, tối thiểu 5 lời gọi, ngưỡng lỗi 50 %, mở 15 s, nửa mở cho 2 lời gọi thử, tự chuyển từ mở sang nửa mở; lời gọi chậm quá 1,5 s được tính là chậm (mở cầu dao khi 80 % lời gọi chậm). Giá trị đọc từ `bookstore.catalog.circuit-breaker.*` (config-server, `order-service.yml`).
- Cầu dao chạy **trên luồng gọi** (`disable-thread-pool=true`, `disable-time-limiter=true`), giao timeout cho `RestClient`, để không mất ngữ cảnh truy vết và lỗi không bị bọc trong `ExecutionException`.
- **404 là lỗi nghiệp vụ**: `UnknownBookException` được `ignoreExceptions` và ném lại nguyên vẹn; mọi lỗi khác (timeout, từ chối kết nối, không còn instance, cầu dao mở) thành `CatalogUnavailableException` → **503**, `type` `upstream-unavailable`, kèm `Retry-After: 10` (ngắn hơn thời gian mở cầu dao 15 s).
- **Khoá chống trùng** `Idempotency-Key` cho `POST /api/v1/orders`: kiểm tra khoá **trước** khi gọi catalog, so băm SHA-256 của khách hàng và các dòng, ràng buộc `UNIQUE (idempotency_key)` trong CSDL xử lý yêu cầu đồng thời. Nhờ đó client có thể gửi lại an toàn khi nhận 503 hoặc mất kết nối.

Phạm vi quyết định này là lời gọi `order-service` → `catalog-service`. Hàng đợi (phương án C) chỉ nên cân nhắc lại cho các tác vụ không cần kết quả ngay, ví dụ thông báo hay đồng bộ tồn kho.

## 4. Hệ quả
- Tích cực: catalog chậm hoặc tắt không còn làm treo order-service; phản hồi lỗi nhanh (9–26 ms khi cầu dao mở, thay vì chờ 10 s); client biết khi nào thử lại nhờ `Retry-After`; gửi lại đơn không sinh đơn trùng và không tốn lời gọi mạng; hợp đồng giữa hai dịch vụ được kiểm chứng tự động.
- Tiêu cực / rủi ro chấp nhận: khi catalog ngừng, **không đặt được đơn mới** (chấp nhận mất sẵn sàng để đổi lấy an toàn và nhất quán giá); ngưỡng cầu dao cần tinh chỉnh theo tải thật; khi cầu dao nửa mở, một số yêu cầu hợp lệ vẫn có thể bị từ chối.
- Việc phải làm tiếp theo: theo dõi số lần chuyển trạng thái cầu dao và tỉ lệ 503 qua metrics; xem xét bộ nhớ đệm giá có thời hạn ngắn nếu catalog hay gián đoạn; dùng hàng đợi nếu sau này có tác vụ không cần kết quả ngay.

## 5. Cách kiểm chứng
- `CatalogCircuitBreakerTest`: WireMock trả chậm 1500 ms, `read-timeout=300ms`, cửa sổ 4, tối thiểu 2 lời gọi. Cả 5 lần `POST /api/v1/orders` đều `503` kèm `Retry-After: 10`, mỗi lần dưới 1400 ms, và WireMock chỉ nhận đúng **2** yêu cầu (3 lần sau bị cầu dao chặn).
- Thử thực tế: tắt catalog, gửi 6 lần liên tiếp đều `503` trong khoảng 9–26 ms; log order-service có dòng `Cầu dao 'catalog' chuyển trạng thái ... CLOSED to OPEN`, sau 15 s có `OPEN to HALF_OPEN`.
- Kiểm thử hợp đồng: `ContractVerifierTest` (catalog-service, 2 ca) và `CatalogClientContractTest` (order-service, 2 ca) bảo đảm 404 `book-not-found` được client hiểu thành `UnknownBookException`, không bị đổi thành 503.
- Idempotency: gửi lại cùng khoá trả cùng đơn với `Idempotent-Replayed: true`; cùng khoá khác nội dung trả `422`; hai yêu cầu đồng thời cùng khoá không sinh `500`.
- Xem xét lại quyết định khi: tỉ lệ 503 do cầu dao mở vượt ngưỡng chấp nhận được trong vận hành, hoặc có yêu cầu nghiệp vụ cho phép nhận đơn khi catalog ngừng (khi đó cân nhắc phương án C).
