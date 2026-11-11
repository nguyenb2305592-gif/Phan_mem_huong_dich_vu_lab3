package vn.edu.bookstore.legacy.adapter.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.bookstore.legacy.adapter.LegacySoapTestServer;

/** Kiểm thử tích hợp adapter ↔ dịch vụ SOAP kế thừa thật (chạy trong JVM kiểm thử). */
@SpringBootTest
@AutoConfigureMockMvc
class LegacyBookControllerTest {

    static final LegacySoapTestServer SOAP = LegacySoapTestServer.start(0);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("bookstore.legacy.endpoint", SOAP::url);
    }

    @AfterAll
    static void stop() {
        SOAP.close();
    }

    @Autowired
    MockMvc mvc;

    @Test
    void layDuocSachTuHeThongKeThua() throws Exception {
        mvc.perform(get("/api/v1/legacy/books/9780134685991"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Effective Java"))
                .andExpect(jsonPath("$.authorId").value(42))
                .andExpect(jsonPath("$.price.amount").value(285000.00))
                .andExpect(jsonPath("$.price.currency").value("VND"))
                .andExpect(jsonPath("$.source").value("legacy-soap"));
    }

    // TH1-8: các nhánh lỗi
    @Test
    void sachKhongTonTaiThanh404BookNotFound() throws Exception {
        mvc.perform(get("/api/v1/legacy/books/9789999999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://api.bookstore.vn/problems/book-not-found"))
                .andExpect(jsonPath("$.legacyCode").value("BOOK_NOT_FOUND"))
                .andExpect(jsonPath("$.instance").value("/api/v1/legacy/books/9789999999999"));
    }

    @Test
    void isbnSaiBiChanTaiAdapter() throws Exception {
        mvc.perform(get("/api/v1/legacy/books/12345"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.bookstore.vn/problems/validation-failed"));
    }

    @Test
    void faultServerThanh502UpstreamUnavailable() throws Exception {
        mvc.perform(get("/api/v1/legacy/books/9790000000000"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.type").value("https://api.bookstore.vn/problems/upstream-unavailable"))
                .andExpect(jsonPath("$.legacyCode").value("BACKEND_ERROR"));
    }

    @Test
    void timKiemTheoTuKhoa() throws Exception {
        mvc.perform(get("/api/v1/legacy/books").param("q", "micro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].isbn").value("9781492034025"));
    }

    @Test
    void tuKhoaQuaNganThanh400() throws Exception {
        mvc.perform(get("/api/v1/legacy/books").param("q", "a"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.bookstore.vn/problems/validation-failed"));
    }
}
