package vn.edu.bookstore.legacy.adapter.soap;

import java.math.BigDecimal;

/** Mô hình nội bộ của adapter cho một cuốn sách lấy từ hệ thống kế thừa. */
public record LegacyBook(String isbn, String title, String author, Integer authorId,
                         Integer publishedYear, BigDecimal amount, String currency) {
}
