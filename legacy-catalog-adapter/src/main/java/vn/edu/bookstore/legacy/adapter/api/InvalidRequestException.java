package vn.edu.bookstore.legacy.adapter.api;

/** Đầu vào không hợp lệ, phát hiện ngay tại adapter (không cần gọi hệ thống kế thừa). */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
