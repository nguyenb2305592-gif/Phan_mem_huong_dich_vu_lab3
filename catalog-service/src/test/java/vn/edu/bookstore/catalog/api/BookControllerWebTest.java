package vn.edu.bookstore.catalog.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.bookstore.catalog.domain.BookEntity;
import vn.edu.bookstore.catalog.service.BookNotFoundException;
import vn.edu.bookstore.catalog.service.BookService;

/** Kiểm thử tầng web (không CSDL): kiểm chứng hợp đồng và ánh xạ lỗi. */
@WebMvcTest(BookController.class)
class BookControllerWebTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    BookService service;

    @Test
    void isbnSaiMauTraVe400() throws Exception {
        mvc.perform(get("/api/v1/books/12345"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.bookstore.vn/problems/validation-failed"));
    }

    @Test
    void sortSaiMauTraVe400() throws Exception {
        mvc.perform(get("/api/v1/books").param("sort", "id;drop"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void khongTimThayTraVe404() throws Exception {
        given(service.get("9789999999999")).willThrow(new BookNotFoundException("9789999999999"));
        mvc.perform(get("/api/v1/books/9789999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Không tìm thấy sách"));
    }

    @Test
    void etagKhopTraVe304() throws Exception {
        given(service.get("9780134685991")).willReturn(new BookEntity("9780134685991", "Effective Java",
                "Joshua Bloch", 42L, 2018, new BigDecimal("285000.00"), "VND"));
        mvc.perform(get("/api/v1/books/9780134685991").header("If-None-Match", "\"0\""))
                .andExpect(status().isNotModified());
    }

    @Test
    void thieuIfMatchTraVe428() throws Exception {
        mvc.perform(put("/api/v1/books/9780134685991").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"t\",\"author\":\"a\",\"price\":{\"amount\":1,\"currency\":\"VND\"}}"))
                .andExpect(status().isPreconditionRequired());
        org.mockito.Mockito.verify(service, org.mockito.Mockito.never()).update(any(), any(), eq(0L));
    }

    @Test
    void anhXaThamSoSort() {
        org.assertj.core.api.Assertions.assertThat(BookController.toSort("price,desc").getOrderFor("priceAmount"))
                .extracting(Sort.Order::getDirection).isEqualTo(Sort.Direction.DESC);
    }
}
