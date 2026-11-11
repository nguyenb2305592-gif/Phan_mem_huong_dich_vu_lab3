package vn.edu.bookstore.order.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerPort;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;
import vn.edu.bookstore.order.TestcontainersConfig;

/**
 * TH3-7: kiểm thử hợp đồng phía BÊN TIÊU THỤ.
 * Stub WireMock được sinh từ hợp đồng của catalog-service (src/test/resources/contracts) và
 * đóng gói thành catalog-service-*-stubs.jar; bên cung cấp đã tự kiểm chứng stub này khi build.
 */
@SpringBootTest
@AutoConfigureStubRunner(ids = "vn.edu.bookstore:catalog-service:+:stubs",
        stubsMode = StubRunnerProperties.StubsMode.CLASSPATH)
@Import(TestcontainersConfig.class)
class CatalogClientContractTest {

    @StubRunnerPort("catalog-service")
    int catalogPort;

    @Autowired
    CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    private CatalogClient client() {
        return new CatalogClient(RestClient.builder().baseUrl("http://localhost:" + catalogPort).build(),
                circuitBreakerFactory);
    }

    @Test
    void docDuocGiaSachTheoHopDong() {
        BookInfo book = client().getBook("9780134685991");
        assertThat(book.title()).isEqualTo("Effective Java");
        assertThat(book.price().amount()).isEqualByComparingTo("285000.00");
        assertThat(book.price().currency()).isEqualTo("VND");
    }

    @Test
    void sachKhongTonTaiTheoHopDong() {
        assertThatThrownBy(() -> client().getBook("9789999999999")).isInstanceOf(UnknownBookException.class);
    }
}
