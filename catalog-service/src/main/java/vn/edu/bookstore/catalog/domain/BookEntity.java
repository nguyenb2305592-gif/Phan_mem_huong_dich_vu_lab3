package vn.edu.bookstore.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "book")
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 13)
    private String isbn;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(name = "author_id")
    private Long authorId;

    @Column(name = "published_year")
    private Integer publishedYear;

    /** Money: BigDecimal + mã tiền tệ, không dùng double. */
    @Column(name = "price_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal priceAmount;

    @Column(name = "price_currency", nullable = false, length = 3)
    private String priceCurrency;

    /** Khoá lạc quan (optimistic locking) — đồng thời là nguồn của ETag. */
    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected BookEntity() {
    }

    public BookEntity(String isbn, String title, String author, Long authorId, Integer publishedYear,
                      BigDecimal priceAmount, String priceCurrency) {
        this.isbn = isbn;
        apply(title, author, authorId, publishedYear, priceAmount, priceCurrency);
    }

    public final void apply(String title, String author, Long authorId, Integer publishedYear,
                            BigDecimal priceAmount, String priceCurrency) {
        this.title = title;
        this.author = author;
        this.authorId = authorId;
        this.publishedYear = publishedYear;
        this.priceAmount = priceAmount;
        this.priceCurrency = priceCurrency;
    }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = OffsetDateTime.now(ZoneOffset.ofHours(7));
    }

    public Long getId() { return id; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public Long getAuthorId() { return authorId; }
    public Integer getPublishedYear() { return publishedYear; }
    public BigDecimal getPriceAmount() { return priceAmount; }
    public String getPriceCurrency() { return priceCurrency; }
    public long getVersion() { return version; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
