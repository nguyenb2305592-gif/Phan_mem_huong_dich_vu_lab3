package vn.edu.bookstore.order.client;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.net.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.loadbalancer.DeferringLoadBalancerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class CatalogClientConfig {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogClientConfig.class);

    /** TH3-2: RestClient có timeout kết nối + timeout đọc; tra Eureka khi load-balanced. */
    @Bean
    RestClient catalogRestClient(RestClient.Builder builder, CatalogClientProperties props,
                                 ObjectProvider<DeferringLoadBalancerInterceptor> loadBalancer) {
        HttpClient http = HttpClient.newBuilder().connectTimeout(props.connectTimeout()).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(props.readTimeout());
        builder.baseUrl(props.baseUrl()).requestFactory(factory);
        if (props.loadBalanced()) {
            loadBalancer.ifAvailable(builder::requestInterceptor);
        }
        return builder.build();
    }

    /** TH3-3: cấu hình cầu dao "catalog" (đọc từ bookstore.catalog.circuit-breaker.*). */
    @Bean
    Customizer<Resilience4JCircuitBreakerFactory> catalogCircuitBreaker(CatalogClientProperties props) {
        CatalogClientProperties.CircuitBreaker cb = props.circuitBreaker();
        return factory -> {
            factory.configure(b -> b
                    .circuitBreakerConfig(CircuitBreakerConfig.custom()
                            .slidingWindowSize(cb.slidingWindowSize())
                            .minimumNumberOfCalls(cb.minimumNumberOfCalls())
                            .failureRateThreshold(cb.failureRateThreshold())
                            .slowCallDurationThreshold(cb.slowCallDurationThreshold())
                            .slowCallRateThreshold(80)
                            .waitDurationInOpenState(cb.waitDurationInOpenState())
                            .permittedNumberOfCallsInHalfOpenState(2)
                            .automaticTransitionFromOpenToHalfOpenEnabled(true)
                            .ignoreExceptions(UnknownBookException.class)
                            .build())
                    .timeLimiterConfig(TimeLimiterConfig.custom()
                            .timeoutDuration(props.readTimeout().plusSeconds(1)).build()), "catalog");
            factory.addCircuitBreakerCustomizer(c -> c.getEventPublisher().onStateTransition(e ->
                    LOG.warn("Cầu dao '{}' chuyển trạng thái {}", e.getCircuitBreakerName(), e.getStateTransition())),
                    "catalog");
        };
    }
}
