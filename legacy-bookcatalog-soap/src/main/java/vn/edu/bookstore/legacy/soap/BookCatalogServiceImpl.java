package vn.edu.bookstore.legacy.soap;

import jakarta.jws.WebService;
import java.math.BigInteger;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import vn.edu.bookstore.legacy.soap.generated.Book;
import vn.edu.bookstore.legacy.soap.generated.BookCatalogPortType;
import vn.edu.bookstore.legacy.soap.generated.CurrencyCode;
import vn.edu.bookstore.legacy.soap.generated.GetBookRequest;
import vn.edu.bookstore.legacy.soap.generated.GetBookResponse;
import vn.edu.bookstore.legacy.soap.generated.Money;
import vn.edu.bookstore.legacy.soap.generated.SearchBooksRequest;
import vn.edu.bookstore.legacy.soap.generated.SearchBooksResponse;

/** Hiện thực BookCatalogPortType (sinh từ WSDL bằng wsimport). */
@WebService(
        serviceName = "BookCatalogService",
        portName = "BookCatalogPort",
        targetNamespace = "http://bookstore.edu.vn/catalog/service/v1",
        endpointInterface = "vn.edu.bookstore.legacy.soap.generated.BookCatalogPortType",
        wsdlLocation = "wsdl/BookCatalogService.wsdl")
public class BookCatalogServiceImpl implements BookCatalogPortType {

    private static final Logger LOG = Logger.getLogger(BookCatalogServiceImpl.class.getName());
    private static final String ISBN_PATTERN = "97[89][0-9]{10}";

    private final LegacyBookStore store;
    private final long delayMillis;

    public BookCatalogServiceImpl(LegacyBookStore store, long delayMillis) {
        this.store = store;
        this.delayMillis = delayMillis;
    }

    @Override
    public GetBookResponse getBook(GetBookRequest request) {
        simulateLatency();
        String isbn = request.getIsbn() == null ? "" : request.getIsbn().trim();
        LOG.info(() -> "GetBook isbn=" + isbn);
        if (LegacyBookStore.ISBN_SIMULATE_SERVER_ERROR.equals(isbn)) {
            throw SoapFaults.server("BACKEND_ERROR", "Kho dữ liệu cũ không phản hồi");
        }
        if (!isbn.matches(ISBN_PATTERN)) {
            throw SoapFaults.client("INVALID_ISBN", "ISBN phải gồm 13 chữ số và bắt đầu bằng 978/979: " + isbn);
        }
        BookRecord record = store.findByIsbn(isbn)
                .orElseThrow(() -> SoapFaults.client("BOOK_NOT_FOUND", "Không tìm thấy sách ISBN " + isbn));
        GetBookResponse response = new GetBookResponse();
        response.setBook(toXml(record));
        return response;
    }

    @Override
    public SearchBooksResponse searchBooks(SearchBooksRequest request) {
        simulateLatency();
        String keyword = request.getKeyword() == null ? "" : request.getKeyword().trim();
        if (keyword.length() < 2) {
            throw SoapFaults.client("INVALID_REQUEST", "Từ khoá tìm kiếm phải có ít nhất 2 ký tự");
        }
        int max = request.getMaxResults() == null ? 10 : Math.max(1, Math.min(50, request.getMaxResults()));
        SearchBooksResponse response = new SearchBooksResponse();
        store.search(keyword, max).forEach(r -> response.getBook().add(toXml(r)));
        return response;
    }

    private void simulateLatency() {
        if (delayMillis > 0) {
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static Book toXml(BookRecord r) {
        Book b = new Book();
        b.setIsbn(r.isbn());
        b.setTitle(r.title());
        b.setAuthor(r.author());
        if (r.authorId() != null) {
            b.setAuthorId(BigInteger.valueOf(r.authorId()));
        }
        try {
            b.setPublishedYear(DatatypeFactory.newInstance().newXMLGregorianCalendarDate(
                    r.publishedYear(), DatatypeConstants.FIELD_UNDEFINED,
                    DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED));
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException(e);
        }
        Money m = new Money();
        m.setAmount(r.amount());
        m.setCurrency(CurrencyCode.fromValue(r.currency()));
        b.setPrice(m);
        return b;
    }
}
