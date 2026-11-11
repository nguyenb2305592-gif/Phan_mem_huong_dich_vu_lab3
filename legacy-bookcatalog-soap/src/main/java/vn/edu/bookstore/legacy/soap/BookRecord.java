package vn.edu.bookstore.legacy.soap;

import java.math.BigDecimal;

/** Bản ghi sách trong "kho dữ liệu cũ" (giả lập bằng bộ nhớ). */
public record BookRecord(String isbn, String title, String author, Integer authorId,
                         int publishedYear, BigDecimal amount, String currency) {
}
