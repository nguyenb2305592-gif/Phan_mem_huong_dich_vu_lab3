package vn.edu.bookstore.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
/** TH3-1: định tuyến của gateway tới một backend giả (HttpServer của JDK). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutingTest {

    static final AtomicReference<String> LAST_PATH = new AtomicReference<>();
    static final AtomicReference<String> LAST_CORRELATION = new AtomicReference<>();
    static final AtomicReference<String> LAST_AUTH = new AtomicReference<>();
    static final HttpServer BACKEND = startBackend();

    static HttpServer startBackend() {
        try {
            HttpServer s = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            s.createContext("/", ex -> {
                LAST_PATH.set(ex.getRequestURI().getPath());
                LAST_CORRELATION.set(ex.getRequestHeaders().getFirst("X-Correlation-Id"));
                LAST_AUTH.set(ex.getRequestHeaders().getFirst("Authorization"));
                byte[] body = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
                ex.getResponseHeaders().add("Content-Type", "application/json");
                ex.sendResponseHeaders(200, body.length);
                ex.getResponseBody().write(body);
                ex.close();
            });
            s.start();
            return s;
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void routes(DynamicPropertyRegistry r) {
        String url = "http://localhost:" + BACKEND.getAddress().getPort();
        r.add("bookstore.routes.catalog", () -> url);
        r.add("bookstore.routes.orders", () -> url);
        r.add("bookstore.routes.inventory", () -> url);
    }

    @AfterAll
    static void stop() {
        BACKEND.stop(0);
    }

    @Value("${local.server.port}")
    int port;

    HttpResponse<String> call(String path, String bearer) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        if (bearer != null) {
            b.header("Authorization", "Bearer " + bearer);
        }
        return HttpClient.newHttpClient().send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void dinhTuyenCatalogVaGanCorrelationId() throws Exception {
        HttpResponse<String> r = call("/api/v1/books/9780134685991", null);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(LAST_PATH.get()).isEqualTo("/api/v1/books/9780134685991");
        assertThat(LAST_CORRELATION.get()).isNotBlank();
        assertThat(r.headers().firstValue("X-Correlation-Id")).contains(LAST_CORRELATION.get());
    }

    // TODO(TH3-1): thêm ca kiểm thử định tuyến /api/v1/orders/1001 sau khi khai báo tuyến orders

    @Test
    void duongDanKhongCoTuyenTraVe404() throws Exception {
        assertThat(call("/khong-co", null).statusCode()).isEqualTo(404);
    }

    @Test
    void dinhTuyenOrders() throws Exception {
        assertThat(call("/api/v1/orders/1001", null).statusCode()).isEqualTo(200);
        assertThat(LAST_PATH.get()).isEqualTo("/api/v1/orders/1001");
    }
}
