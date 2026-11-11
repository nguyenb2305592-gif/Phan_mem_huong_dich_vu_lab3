package vn.edu.bookstore.order.service;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("Không tìm thấy đơn hàng " + id);
    }
}
