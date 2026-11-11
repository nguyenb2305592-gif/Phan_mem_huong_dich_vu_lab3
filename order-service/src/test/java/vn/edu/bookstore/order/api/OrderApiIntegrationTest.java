package vn.edu.bookstore.order.api;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.bookstore.order.TestcontainersConfig;
import vn.edu.bookstore.order.support.CatalogStub;

/** TH3-6: order-service + PostgreSQL thật + catalog giả lập bằng WireMock. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class OrderApiIntegrationTest {

    static final WireMockServer CATALOG = CatalogStub.start();

    static {
        CatalogStub.book(CATALOG, "9780134685991", "Effective Java", "285000.00", "VND");
        CatalogStub.book(CATALOG, "9781617294549", "Microservices Patterns", "520000.00", "VND");
        CatalogStub.book(CATALOG, "9780321125217", "Domain-Driven Design", "39.99", "USD");
        CatalogStub.notFound(CATALOG, "9789999999999");
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("bookstore.catalog.base-url", CATALOG::baseUrl);
    }

    @AfterAll
    static void stop() {
        CATALOG.stop();
    }

    @Autowired
    MockMvc mvc;

    static final String ORDER = """
            {"customerId":88,"lines":[{"isbn":"9780134685991","quantity":1},{"isbn":"9781617294549","quantity":2}]}""";

    @Test
    void datHangTinhTongTienVaSinhMaDon() throws Exception {
        mvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(ORDER))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.customerId").value(88))
                .andExpect(jsonPath("$.total.amount").value(1325000.00))
                .andExpect(jsonPath("$.total.currency").value("VND"))
                .andExpect(jsonPath("$.orderNumber").value(matchesPattern("ORD-[0-9]{4}-[0-9]{6}")));
    }

    @Test
    void cungIdempotencyKeyKhongTaoDonThuHai() throws Exception {
        String first = mvc.perform(post("/api/v1/orders").header("Idempotency-Key", "k-th3-0001")
                        .contentType(MediaType.APPLICATION_JSON).content(ORDER))
                .andExpect(status().isCreated())
                .andExpect(header().string("Idempotent-Replayed", "false"))
                .andReturn().getResponse().getContentAsString();
        String id = first.replaceAll(".*\"id\":(\\d+).*", "$1");
        mvc.perform(post("/api/v1/orders").header("Idempotency-Key", "k-th3-0001")
                        .contentType(MediaType.APPLICATION_JSON).content(ORDER))
                .andExpect(status().isCreated())
                .andExpect(header().string("Idempotent-Replayed", "true"))
                .andExpect(jsonPath("$.id").value(Long.parseLong(id)));
        // cùng khoá nhưng nội dung khác → 422
        mvc.perform(post("/api/v1/orders").header("Idempotency-Key", "k-th3-0001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":88,\"lines\":[{\"isbn\":\"9780134685991\",\"quantity\":5}]}"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void sachKhongCoTrongDanhMuc() throws Exception {
        mvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":88,\"lines\":[{\"isbn\":\"9789999999999\",\"quantity\":1}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.bookstore.vn/problems/validation-failed"))
                .andExpect(jsonPath("$.isbn").value("9789999999999"));
    }

    @Test
    void khacLoaiTienTeBiTuChoi() throws Exception {
        mvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":88,\"lines\":[{\"isbn\":\"9780134685991\",\"quantity\":1},{\"isbn\":\"9780321125217\",\"quantity\":1}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void thanYeuCauSaiHopDong() throws Exception {
        mvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":88,\"lines\":[{\"isbn\":\"123\",\"quantity\":0}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.bookstore.vn/problems/validation-failed"));
    }

    @Test
    void docDonHang() throws Exception {
        String body = mvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(ORDER))
                .andReturn().getResponse().getContentAsString();
        String id = body.replaceAll(".*\"id\":(\\d+).*", "$1");
        mvc.perform(get("/api/v1/orders/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines.length()").value(2));
        mvc.perform(get("/api/v1/orders/999999")).andExpect(status().isNotFound());
    }
}
