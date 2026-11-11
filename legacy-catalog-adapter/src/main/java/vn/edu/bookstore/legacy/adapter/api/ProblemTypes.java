package vn.edu.bookstore.legacy.adapter.api;

import java.net.URI;

/** Các kiểu lỗi RFC 9457 dùng chung của BookStore (mục 9.5 đặc tả). */
public final class ProblemTypes {

    private static final String BASE = "https://api.bookstore.vn/problems/";
    public static final URI BOOK_NOT_FOUND = URI.create(BASE + "book-not-found");
    public static final URI VALIDATION_FAILED = URI.create(BASE + "validation-failed");
    public static final URI UPSTREAM_UNAVAILABLE = URI.create(BASE + "upstream-unavailable");

    private ProblemTypes() {
    }
}
