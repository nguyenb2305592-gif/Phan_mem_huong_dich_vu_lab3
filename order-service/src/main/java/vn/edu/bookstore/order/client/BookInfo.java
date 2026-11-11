package vn.edu.bookstore.order.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/**
 * Chỉ những trường order-service cần từ catalog-service (bên tiêu thụ đọc dung sai —
 * bỏ qua trường lạ để catalog thêm trường mới không làm vỡ order-service).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BookInfo(String isbn, String title, Price price) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Price(BigDecimal amount, String currency) {
    }
}
