package vn.edu.bookstore.catalog.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.bookstore.catalog.TestcontainersConfig;

/** TH2-7: kiểm thử tích hợp với PostgreSQL thật (Testcontainers) + Flyway. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class BookApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    private static final String NEW_BOOK = """
            {"isbn":"%s","title":"Software Architecture: The Hard Parts","author":"Neal Ford",
             "publishedYear":2021,"price":{"amount":640000.00,"currency":"VND"}}""";

    @Test
    void phanTrangVaSapXep() throws Exception {
        mvc.perform(get("/api/v1/books").param("page", "0").param("size", "3").param("sort", "price,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.page.size").value(3))
                .andExpect(jsonPath("$.page.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(8)))
                .andExpect(jsonPath("$.content[0].title").value("Enterprise Integration Patterns"));
    }

    @Test
    void timTheoTuKhoa() throws Exception {
        mvc.perform(get("/api/v1/books").param("q", "microservices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(3));
    }

    @Test
    void sizeVuotGioiHanBiTuChoi() throws Exception {
        mvc.perform(get("/api/v1/books").param("size", "1000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.bookstore.vn/problems/validation-failed"));
    }

    @Test
    void layTheoIsbnCoEtagVa304() throws Exception {
        String etag = mvc.perform(get("/api/v1/books/9780134685991"))
                .andExpect(status().isOk())
                .andExpect(header().exists("ETag"))
                .andExpect(jsonPath("$.price.amount").value(285000.00))
                .andExpect(jsonPath("$.authorId").value(42))
                .andReturn().getResponse().getHeader("ETag");
        mvc.perform(get("/api/v1/books/9780134685991").header("If-None-Match", etag))
                .andExpect(status().isNotModified());
    }

    @Test
    void sachKhongTonTaiTraVeProblem() throws Exception {
        mvc.perform(get("/api/v1/books/9789999999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://api.bookstore.vn/problems/book-not-found"))
                .andExpect(jsonPath("$.instance").value("/api/v1/books/9789999999999"));
    }

    @Test
    void taoCapNhatXoaVoiKhoaLacQuan() throws Exception {
        String isbn = "9781098100131";
        String etag = mvc.perform(post("/api/v1/books").contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_BOOK.formatted(isbn)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/books/" + isbn)))
                .andReturn().getResponse().getHeader("ETag");

        mvc.perform(post("/api/v1/books").contentType(MediaType.APPLICATION_JSON)
                        .content(NEW_BOOK.formatted(isbn)))
                .andExpect(status().isConflict());

        String update = """
                {"title":"Software Architecture: The Hard Parts (2nd)","author":"Neal Ford",
                 "price":{"amount":650000.00,"currency":"VND"}}""";
        mvc.perform(put("/api/v1/books/" + isbn).contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isPreconditionRequired());
        String etag2 = mvc.perform(put("/api/v1/books/" + isbn).header("If-Match", etag)
                        .contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price.amount").value(650000.00))
                .andReturn().getResponse().getHeader("ETag");
        org.assertj.core.api.Assertions.assertThat(etag2).isNotEqualTo(etag);
        // ghi bằng ETag cũ → 412 (mất cập nhật bị chặn)
        mvc.perform(put("/api/v1/books/" + isbn).header("If-Match", etag)
                        .contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isPreconditionFailed());

        mvc.perform(delete("/api/v1/books/" + isbn)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/books/" + isbn)).andExpect(status().isNotFound());
    }

    @Test
    void duLieuSaiHopDongBiTuChoi() throws Exception {
        String bad = """
                {"isbn":"123","title":"","author":"X","price":{"amount":-5,"currency":"EUR"}}""";
        mvc.perform(post("/api/v1/books").contentType(MediaType.APPLICATION_JSON).content(bad))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.bookstore.vn/problems/validation-failed"));
    }
}
