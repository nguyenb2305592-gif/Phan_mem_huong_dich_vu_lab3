package vn.edu.bookstore.catalog.api;

import org.springframework.data.domain.Page;
import vn.edu.bookstore.catalog.api.model.Book;
import vn.edu.bookstore.catalog.api.model.BookPage;
import vn.edu.bookstore.catalog.api.model.CurrencyCode;
import vn.edu.bookstore.catalog.api.model.Money;
import vn.edu.bookstore.catalog.api.model.PageMetadata;
import vn.edu.bookstore.catalog.domain.BookEntity;

/** Ánh xạ thực thể JPA ↔ model sinh từ OpenAPI (không trả thực thể JPA ra ngoài). */
final class BookMapper {

    private BookMapper() {
    }

    static Book toApi(BookEntity e) {
        Book b = new Book(e.getIsbn(), e.getTitle(), e.getAuthor(),
                new Money(e.getPriceAmount(), CurrencyCode.fromValue(e.getPriceCurrency())));
        b.setAuthorId(e.getAuthorId());
        b.setPublishedYear(e.getPublishedYear());
        return b;
    }

    static BookPage toPage(Page<BookEntity> page) {
        return new BookPage(page.map(BookMapper::toApi).getContent(),
                new PageMetadata(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()));
    }

    static String etag(BookEntity e) {
        return "\"" + e.getVersion() + "\"";
    }
}
