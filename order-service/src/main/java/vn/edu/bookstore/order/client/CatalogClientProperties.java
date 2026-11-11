package vn.edu.bookstore.order.client;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cấu hình bên tiêu thụ catalog-service (đặt ngoài mã, ghi đè qua config-server/biến môi trường).
 * base-url = http://catalog-service + load-balanced = true → tra Eureka; tắt khi kiểm thử với WireMock.
 */
@ConfigurationProperties(prefix = "bookstore.catalog")
public record CatalogClientProperties(
        String baseUrl,
        Boolean loadBalanced,
        Duration connectTimeout,
        Duration readTimeout,
        CircuitBreaker circuitBreaker) {

    public CatalogClientProperties {
        baseUrl = baseUrl == null ? "http://catalog-service" : baseUrl;
        loadBalanced = loadBalanced == null ? Boolean.TRUE : loadBalanced;
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(1) : connectTimeout;
        readTimeout = readTimeout == null ? Duration.ofSeconds(2) : readTimeout;
        circuitBreaker = circuitBreaker == null ? new CircuitBreaker(null, null, null, null, null) : circuitBreaker;
    }

    public record CircuitBreaker(Integer slidingWindowSize, Integer minimumNumberOfCalls,
                                 Float failureRateThreshold, Duration slowCallDurationThreshold,
                                 Duration waitDurationInOpenState) {
        public CircuitBreaker {
            slidingWindowSize = slidingWindowSize == null ? 10 : slidingWindowSize;
            minimumNumberOfCalls = minimumNumberOfCalls == null ? 5 : minimumNumberOfCalls;
            failureRateThreshold = failureRateThreshold == null ? 50f : failureRateThreshold;
            slowCallDurationThreshold = slowCallDurationThreshold == null ? Duration.ofMillis(1500) : slowCallDurationThreshold;
            waitDurationInOpenState = waitDurationInOpenState == null ? Duration.ofSeconds(15) : waitDurationInOpenState;
        }
    }
}
