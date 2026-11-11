package vn.edu.bookstore.legacy.adapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LegacyCatalogAdapterApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegacyCatalogAdapterApplication.class, args);
    }
}
