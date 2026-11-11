package vn.edu.bookstore.catalog.service;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(String isbn) {
        super("Không có sách ISBN " + isbn);
    }
}
