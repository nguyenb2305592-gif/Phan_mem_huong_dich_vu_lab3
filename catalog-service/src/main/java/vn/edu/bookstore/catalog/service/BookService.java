package vn.edu.bookstore.catalog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.bookstore.catalog.api.model.BookCreateRequest;
import vn.edu.bookstore.catalog.api.model.BookUpdateRequest;
import vn.edu.bookstore.catalog.domain.BookEntity;
import vn.edu.bookstore.catalog.domain.BookRepository;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public Page<BookEntity> list(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return repository.findAll(pageable);
        }
        return repository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(q.trim(), q.trim(), pageable);
    }

    public BookEntity get(String isbn) {
        return repository.findByIsbn(isbn).orElseThrow(() -> new BookNotFoundException(isbn));
    }

    @Transactional
    public BookEntity create(BookCreateRequest r) {
        if (repository.existsByIsbn(r.getIsbn())) {
            throw new DuplicateIsbnException(r.getIsbn());
        }
        BookEntity e = new BookEntity(r.getIsbn(), r.getTitle(), r.getAuthor(), r.getAuthorId(),
                r.getPublishedYear(), r.getPrice().getAmount(), r.getPrice().getCurrency().getValue());
        return repository.saveAndFlush(e);
    }

    /** Cập nhật có điều kiện: chỉ ghi khi phiên bản client đang giữ trùng phiên bản hiện tại. */
    @Transactional
    public BookEntity update(String isbn, BookUpdateRequest r, long expectedVersion) {
        BookEntity e = get(isbn);
        if (e.getVersion() != expectedVersion) {
            throw new PreconditionFailedException(
                    "ETag \"" + expectedVersion + "\" đã cũ, phiên bản hiện tại là \"" + e.getVersion() + "\"");
        }
        e.apply(r.getTitle(), r.getAuthor(), r.getAuthorId(), r.getPublishedYear(),
                r.getPrice().getAmount(), r.getPrice().getCurrency().getValue());
        return repository.saveAndFlush(e);
    }

    @Transactional
    public void delete(String isbn) {
        repository.delete(get(isbn));
    }
}
