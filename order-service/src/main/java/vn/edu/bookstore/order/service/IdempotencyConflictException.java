package vn.edu.bookstore.order.service;

public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException(String key) {
        super("Idempotency-Key " + key + " đã dùng cho một yêu cầu khác");
    }
}
