package vn.edu.bookstore.order.service;

import vn.edu.bookstore.order.api.OrderView;

/** replayed = true khi yêu cầu trùng Idempotency-Key và được trả lại kết quả cũ. */
public record PlaceOrderResult(OrderView order, boolean replayed) {
}
