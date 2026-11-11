package vn.edu.bookstore.order.client;

/** catalog-service chậm/tắt/lỗi 5xx hoặc cầu dao đang mở. */
public class CatalogUnavailableException extends RuntimeException {

    public CatalogUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
