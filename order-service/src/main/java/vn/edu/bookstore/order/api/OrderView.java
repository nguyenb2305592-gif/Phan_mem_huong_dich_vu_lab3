package vn.edu.bookstore.order.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import vn.edu.bookstore.order.domain.OrderEntity;

public record OrderView(Long id, String orderNumber, Long customerId, String status, Money total,
                        OffsetDateTime createdAt, List<Line> lines) {

    public record Money(BigDecimal amount, String currency) {
    }

    public record Line(String isbn, int quantity, Money unitPrice) {
    }

    public static OrderView from(OrderEntity o) {
        return new OrderView(o.getId(), o.getOrderNumber(), o.getCustomerId(), o.getStatus().name(),
                new Money(o.getTotalAmount(), o.getCurrency()), o.getCreatedAt(),
                o.getLines().stream()
                        .map(l -> new Line(l.getIsbn(), l.getQuantity(), new Money(l.getUnitPrice(), l.getCurrency())))
                        .toList());
    }
}
