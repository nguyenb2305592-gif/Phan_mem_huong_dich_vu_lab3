package vn.edu.bookstore.contracts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.MarshalException;
import java.math.BigDecimal;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import vn.edu.bookstore.contracts.catalog.Book;
import vn.edu.bookstore.contracts.catalog.CurrencyCode;
import vn.edu.bookstore.contracts.catalog.Money;
import vn.edu.bookstore.contracts.catalog.ObjectFactory;
import vn.edu.bookstore.contracts.order.Order;
import vn.edu.bookstore.contracts.order.OrderStatus;

class XsdValidationTest {

    private static StreamSource sample(String name) {
        return new StreamSource(XsdValidationTest.class.getResourceAsStream("/samples/" + name));
    }

    @Test
    void sachHopLeQuaKiemChung() {
        assertThat(XmlContracts.validate(sample("book-valid.xml"))).isEmpty();
    }

    @Test
    void donHangHopLeQuaKiemChung() {
        assertThat(XmlContracts.validate(sample("order-valid.xml"))).isEmpty();
    }

    // TH1-3: mỗi tệp dưới đây vi phạm đúng một ràng buộc của lược đồ
    @ParameterizedTest
    @ValueSource(strings = {
            "book-invalid-isbn.xml",
            "book-invalid-isbn-length.xml",
            "book-invalid-money-negative.xml",
            "book-invalid-money-scale.xml",
            "book-invalid-currency.xml",
            "book-wrong-namespace.xml",
            "order-invalid-number.xml",
            "order-invalid-status.xml"})
    void tepViPhamLuocDoBiTuChoi(String file) {
        List<String> errors = XmlContracts.validate(sample(file));
        assertThat(errors).as("lỗi kiểm chứng của " + file).isNotEmpty();
    }

    @Test
    void tepKhongDungDangBaoLoiDungDang() {
        List<String> errors = XmlContracts.validate(sample("book-not-well-formed.xml"));
        assertThat(errors).isNotEmpty();
        assertThat(String.join(" ", errors)).containsIgnoringCase("title");
    }

    @Test
    void docXmlThanhDoiTuongGiuDungBigDecimal() throws Exception {
        Book book = XmlContracts.unmarshal(sample("book-valid.xml"), Book.class);
        assertThat(book.getIsbn()).isEqualTo("9780134685991");
        assertThat(book.getPrice().getAmount()).isEqualByComparingTo(new BigDecimal("285000"));
        assertThat(book.getPrice().getCurrency()).isEqualTo(CurrencyCode.VND);

        Order order = XmlContracts.unmarshal(sample("order-valid.xml"), Order.class);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(order.getLine()).hasSize(2);
        assertThat(order.getOrderDate().getTimezone()).isEqualTo(7 * 60);
    }

    @Test
    void ghiDoiTuongHopLeRaXml() throws Exception {
        Book book = new Book();
        book.setIsbn("9781617294549");
        book.setTitle("Microservices Patterns");
        book.setAuthor("Chris Richardson");
        Money price = new Money();
        price.setAmount(new BigDecimal("520000.00"));
        price.setCurrency(CurrencyCode.VND);
        book.setPrice(price);
        String xml = XmlContracts.marshal(new ObjectFactory().createBook(book));
        assertThat(xml).contains("isbn>9781617294549</")
                .contains("http://bookstore.edu.vn/catalog/v1");
    }

    @Test
    void ghiDoiTuongViPhamLuocDoBiChan() {
        Book book = new Book();
        book.setIsbn("123");            // sai mẫu Isbn13
        book.setTitle("Sách lỗi");
        book.setAuthor("Không rõ");
        Money price = new Money();
        price.setAmount(new BigDecimal("1.999"));
        price.setCurrency(CurrencyCode.USD);
        book.setPrice(price);
        JAXBElement<Book> el = new JAXBElement<>(
                new QName("http://bookstore.edu.vn/catalog/v1", "Book"), Book.class, book);
        assertThatThrownBy(() -> XmlContracts.marshal(el)).isInstanceOf(MarshalException.class);
    }
}
