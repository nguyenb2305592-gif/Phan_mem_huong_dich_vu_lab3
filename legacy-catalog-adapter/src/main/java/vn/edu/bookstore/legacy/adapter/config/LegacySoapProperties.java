package vn.edu.bookstore.legacy.adapter.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cấu hình ngoài cho client SOAP (không viết cứng địa chỉ/timeout trong mã). */
@ConfigurationProperties(prefix = "bookstore.legacy")
public record LegacySoapProperties(
        String endpoint,
        Duration connectTimeout,
        Duration readTimeout,
        boolean logMessages) {

    public LegacySoapProperties {
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = "http://localhost:8091/ws/catalog";
        }
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(2) : connectTimeout;
        readTimeout = readTimeout == null ? Duration.ofSeconds(3) : readTimeout;
    }
}
