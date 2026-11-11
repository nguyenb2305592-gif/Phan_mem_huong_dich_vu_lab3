package vn.edu.bookstore.order.client;

/** catalog-service trả 404: lỗi nghiệp vụ, KHÔNG tính là lỗi hạ tầng của cầu dao. */
public class UnknownBookException extends RuntimeException {

    private final String isbn;

    public UnknownBookException(String isbn) {
        super("Sách ISBN " + isbn + " không có trong danh mục");
        this.isbn = isbn;
    }

    public String isbn() {
        return isbn;
    }
}
