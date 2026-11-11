package vn.edu.bookstore.legacy.soap;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Dữ liệu mẫu của hệ thống kế thừa (khớp mục 9.4 của đặc tả học phần). */
public final class LegacyBookStore {

    /** ISBN đặc biệt mô phỏng lỗi phía máy chủ (CSDL cũ không phản hồi). */
    public static final String ISBN_SIMULATE_SERVER_ERROR = "9790000000000";

    private final Map<String, BookRecord> books = List.of(
            new BookRecord("9780134685991", "Effective Java", "Joshua Bloch", 42, 2018,
                    new BigDecimal("285000.00"), "VND"),
            new BookRecord("9781617294549", "Microservices Patterns", "Chris Richardson", null, 2018,
                    new BigDecimal("520000.00"), "VND"),
            new BookRecord("9781492034025", "Building Microservices", "Sam Newman", null, 2021,
                    new BigDecimal("690000.00"), "VND"),
            new BookRecord("9781449373320", "Designing Data-Intensive Applications", "Martin Kleppmann", null, 2017,
                    new BigDecimal("750000.00"), "VND"),
            new BookRecord("9780321125217", "Domain-Driven Design", "Eric Evans", null, 2003,
                    new BigDecimal("39.99"), "USD"))
            .stream().collect(Collectors.toUnmodifiableMap(BookRecord::isbn, Function.identity()));

    public Optional<BookRecord> findByIsbn(String isbn) {
        return Optional.ofNullable(books.get(isbn));
    }

    public List<BookRecord> search(String keyword, int max) {
        String k = keyword.toLowerCase(Locale.ROOT);
        return books.values().stream()
                .filter(b -> b.title().toLowerCase(Locale.ROOT).contains(k)
                        || b.author().toLowerCase(Locale.ROOT).contains(k))
                .sorted(java.util.Comparator.comparing(BookRecord::isbn))
                .limit(max)
                .toList();
    }
}
