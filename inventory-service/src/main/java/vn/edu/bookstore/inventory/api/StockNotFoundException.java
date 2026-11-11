package vn.edu.bookstore.inventory.api;

public class StockNotFoundException extends RuntimeException {

    public StockNotFoundException(String isbn) {
        super("Chưa có tồn kho cho ISBN " + isbn);
    }
}
