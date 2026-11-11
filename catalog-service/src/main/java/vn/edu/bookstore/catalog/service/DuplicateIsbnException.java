package vn.edu.bookstore.catalog.service;

public class DuplicateIsbnException extends RuntimeException {

    public DuplicateIsbnException(String isbn) {
        super("Đã tồn tại sách ISBN " + isbn);
    }
}
