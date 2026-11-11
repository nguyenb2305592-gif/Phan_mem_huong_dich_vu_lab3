package vn.edu.bookstore.legacy.adapter.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.bookstore.legacy.adapter.LegacySoapTestServer;

/** Hệ thống kế thừa tắt (không có ai lắng nghe cổng) → 503. */
@SpringBootTest
@AutoConfigureMockMvc
class LegacyDownTest {

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        int port = LegacySoapTestServer.freePort();
        r.add("bookstore.legacy.endpoint", () -> "http://localhost:" + port + "/ws/catalog");
    }

    @Autowired
    MockMvc mvc;

    @Test
    void heThongKeThuaTatThanh503() throws Exception {
        mvc.perform(get("/api/v1/legacy/books/9780134685991"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.type").value("https://api.bookstore.vn/problems/upstream-unavailable"));
    }
}
