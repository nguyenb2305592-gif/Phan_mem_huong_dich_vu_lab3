package vn.edu.bookstore.legacy.adapter.soap;

/**
 * Lỗi đã được "dịch" từ hệ thống kế thừa sang mô hình của BookStore.
 * Bên trong adapter không để lộ kiểu ngoại lệ của SOAP ra tầng REST.
 */
public class LegacyCatalogException extends RuntimeException {

    public enum Kind { NOT_FOUND, INVALID_REQUEST, UPSTREAM_ERROR, UPSTREAM_UNAVAILABLE, UPSTREAM_TIMEOUT }

    private final Kind kind;
    private final String legacyCode;

    public LegacyCatalogException(Kind kind, String legacyCode, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
        this.legacyCode = legacyCode;
    }

    public Kind kind() {
        return kind;
    }

    public String legacyCode() {
        return legacyCode;
    }
}
