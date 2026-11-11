package vn.edu.bookstore.inventory.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.bookstore.inventory.TestcontainersConfig;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class StockApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void docTonKho() throws Exception {
        mvc.perform(get("/api/v1/inventory/9780134685991"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(10));
    }

    @Test
    void isbnChuaCoTonKho() throws Exception {
        mvc.perform(get("/api/v1/inventory/9789999999999")).andExpect(status().isNotFound());
    }

    @Test
    void dieuChinhTonKho() throws Exception {
        mvc.perform(put("/api/v1/inventory/9781098100131").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"available\": 7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(7));
        mvc.perform(put("/api/v1/inventory/9781098100131").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"available\": -1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://api.bookstore.vn/problems/validation-failed"));
    }
}
