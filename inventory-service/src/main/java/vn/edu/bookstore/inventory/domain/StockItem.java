package vn.edu.bookstore.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "stock_item")
public class StockItem {

    @Id
    @Column(length = 13)
    private String isbn;

    @Column(nullable = false)
    private int available;

    @Column(nullable = false)
    private int reserved;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected StockItem() {
    }

    public StockItem(String isbn, int available) {
        this.isbn = isbn;
        this.available = available;
    }

    /** Giữ hàng: chuyển từ available sang reserved; trả false nếu không đủ. */
    public boolean reserve(int quantity) {
        if (quantity <= 0 || available < quantity) {
            return false;
        }
        available -= quantity;
        reserved += quantity;
        return true;
    }

    /** Trả hàng đã giữ về available (bù trừ). */
    public void release(int quantity) {
        int q = Math.min(quantity, reserved);
        reserved -= q;
        available += q;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = OffsetDateTime.now(ZoneOffset.ofHours(7));
    }

    public String getIsbn() { return isbn; }
    public int getAvailable() { return available; }
    public int getReserved() { return reserved; }
    public long getVersion() { return version; }
}
