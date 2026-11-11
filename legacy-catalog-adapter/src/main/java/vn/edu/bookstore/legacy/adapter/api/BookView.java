package vn.edu.bookstore.legacy.adapter.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import vn.edu.bookstore.legacy.adapter.soap.LegacyBook;

/** Biểu diễn JSON trả cho bên tiêu thụ REST. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookView(String isbn, String title, String author, Integer authorId,
                       Integer publishedYear, Money price, String source) {

    public record Money(BigDecimal amount, String currency) {
    }

    static BookView from(LegacyBook b) {
        return new BookView(b.isbn(), b.title(), b.author(), b.authorId(), b.publishedYear(),
                new Money(b.amount(), b.currency()), "legacy-soap");
    }
}
