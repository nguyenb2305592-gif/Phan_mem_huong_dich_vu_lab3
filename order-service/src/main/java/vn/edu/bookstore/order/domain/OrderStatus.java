package vn.edu.bookstore.order.domain;

/** Trạng thái đơn trong Saga (mục 9.4 đặc tả). TH3 chỉ dùng PENDING; TH5 bổ sung chuyển trạng thái. */
public enum OrderStatus {
    PENDING, STOCK_RESERVED, PAID, CONFIRMED, CANCELLED
}
