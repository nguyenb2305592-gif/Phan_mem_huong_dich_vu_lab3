package vn.edu.bookstore.legacy.adapter.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import vn.edu.bookstore.legacy.adapter.soap.LegacyCatalogClient;

/**
 * Chạy với profile "demo" để gọi thử GetBook/SearchBooks và đọc thông điệp SOAP trong log:
 *   ./mvnw -pl legacy-catalog-adapter spring-boot:run -Dspring-boot.run.profiles=demo
 */
@Component
@Profile("demo")
class SoapClientDemo implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(SoapClientDemo.class);
    private final LegacyCatalogClient client;

    SoapClientDemo(LegacyCatalogClient client) {
        this.client = client;
    }

    @Override
    public void run(String... args) {
        LOG.info("GetBook → {}", client.getBook("9780134685991"));
        LOG.info("SearchBooks('micro') → {}", client.search("micro", 5));
        try {
            client.getBook("9789999999999");
        } catch (RuntimeException e) {
            LOG.info("GetBook sách không tồn tại → {}", e.getMessage());
        }
    }
}
