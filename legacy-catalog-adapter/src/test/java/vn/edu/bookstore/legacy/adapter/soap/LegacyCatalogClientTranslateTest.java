package vn.edu.bookstore.legacy.adapter.soap;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import vn.edu.bookstore.legacy.adapter.soap.LegacyCatalogException.Kind;
import vn.edu.bookstore.legacy.adapter.soap.generated.BookCatalogFault;
import vn.edu.bookstore.legacy.adapter.soap.generated.ErrorDetail;

/** Kiểm thử đơn vị cho bảng dịch mã lỗi kế thừa → loại lỗi nội bộ. */
class LegacyCatalogClientTranslateTest {

    private static BookCatalogFault fault(String code) {
        ErrorDetail d = new ErrorDetail();
        d.setCode(code);
        d.setMessage("m");
        return new BookCatalogFault("m", d);
    }

    @Test
    void bangDichMaLoi() {
        assertThat(LegacyCatalogClient.translate(fault("BOOK_NOT_FOUND")).kind()).isEqualTo(Kind.NOT_FOUND);
        assertThat(LegacyCatalogClient.translate(fault("INVALID_ISBN")).kind()).isEqualTo(Kind.INVALID_REQUEST);
        assertThat(LegacyCatalogClient.translate(fault("INVALID_REQUEST")).kind()).isEqualTo(Kind.INVALID_REQUEST);
        assertThat(LegacyCatalogClient.translate(fault("BACKEND_ERROR")).kind()).isEqualTo(Kind.UPSTREAM_ERROR);
        assertThat(LegacyCatalogClient.translate(fault("MA_LA")).kind()).isEqualTo(Kind.UPSTREAM_ERROR);
    }
}
