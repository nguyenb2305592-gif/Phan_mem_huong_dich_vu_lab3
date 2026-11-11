package vn.edu.bookstore.order.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    /** Cấp id kế tiếp cho đơn hàng (orderNumber = ORD-<năm>-<id 6 chữ số>, ví dụ ORD-2026-001001). */
    @Query(value = "select nextval('orders_seq')", nativeQuery = true)
    long nextOrderId();

    Optional<OrderEntity> findByIdempotencyKey(String idempotencyKey);

    List<OrderEntity> findByCustomerIdOrderByIdDesc(Long customerId);
}
