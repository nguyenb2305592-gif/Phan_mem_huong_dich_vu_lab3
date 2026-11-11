package vn.edu.bookstore.catalog.contract;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.edu.bookstore.catalog.api.BookController;
import vn.edu.bookstore.catalog.api.error.GlobalProblemHandler;
import vn.edu.bookstore.catalog.domain.BookEntity;
import vn.edu.bookstore.catalog.service.BookNotFoundException;
import vn.edu.bookstore.catalog.service.BookService;

/**
 * Lớp cơ sở cho kiểm thử sinh từ hợp đồng (target/generated-test-sources/contracts).
 * Chỉ dựng tầng web với dịch vụ giả lập: hợp đồng kiểm tra HÌNH DẠNG giao tiếp, không kiểm tra CSDL.
 */
public abstract class ContractVerifierBase {

    @BeforeEach
    void setup() {
        BookService service = mock(BookService.class);
        given(service.get("9780134685991")).willReturn(new BookEntity("9780134685991", "Effective Java",
                "Joshua Bloch", 42L, 2018, new BigDecimal("285000.00"), "VND"));
        given(service.get("9789999999999")).willThrow(new BookNotFoundException("9789999999999"));
        RestAssuredMockMvc.standaloneSetup(MockMvcBuilders.standaloneSetup(new BookController(service))
                .setControllerAdvice(new GlobalProblemHandler()));
    }
}
