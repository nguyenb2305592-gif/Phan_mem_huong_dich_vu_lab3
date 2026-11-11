package vn.edu.bookstore.legacy.adapter.soap;

import jakarta.xml.soap.SOAPFault;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.SOAPFaultException;
import java.net.SocketTimeoutException;
import java.util.List;
import org.springframework.stereotype.Component;
import vn.edu.bookstore.legacy.adapter.soap.LegacyCatalogException.Kind;
import vn.edu.bookstore.legacy.adapter.soap.generated.Book;
import vn.edu.bookstore.legacy.adapter.soap.generated.BookCatalogFault;
import vn.edu.bookstore.legacy.adapter.soap.generated.BookCatalogPortType;
import vn.edu.bookstore.legacy.adapter.soap.generated.ErrorDetail;
import vn.edu.bookstore.legacy.adapter.soap.generated.GetBookRequest;
import vn.edu.bookstore.legacy.adapter.soap.generated.SearchBooksRequest;

/**
 * Lớp chống lệch mô hình (anti-corruption layer, ACL): gọi BookCatalogService và chuyển
 * kiểu dữ liệu + lỗi SOAP sang mô hình nội bộ {@link LegacyBook} / {@link LegacyCatalogException}.
 */
@Component
public class LegacyCatalogClient {

    private final BookCatalogPortType port;

    public LegacyCatalogClient(BookCatalogPortType port) {
        this.port = port;
    }

    public LegacyBook getBook(String isbn) {
        GetBookRequest request = new GetBookRequest();
        request.setIsbn(isbn);
        try {
            return toModel(port.getBook(request).getBook());
        } catch (BookCatalogFault fault) {
            throw translate(fault);
        } catch (SOAPFaultException e) {
            throw translate(e);
        } catch (WebServiceException e) {
            throw unavailable(e);
        }
    }

    public List<LegacyBook> search(String keyword, int maxResults) {
        SearchBooksRequest request = new SearchBooksRequest();
        request.setKeyword(keyword);
        request.setMaxResults(maxResults);
        try {
            return port.searchBooks(request).getBook().stream().map(LegacyCatalogClient::toModel).toList();
        } catch (BookCatalogFault fault) {
            throw translate(fault);
        } catch (SOAPFaultException e) {
            throw translate(e);
        } catch (WebServiceException e) {
            throw unavailable(e);
        }
    }

    /** TH1-7: fault có mô hình (khai báo trong WSDL) — dựa vào mã nghiệp vụ trong bs:ErrorDetail. */
    static LegacyCatalogException translate(BookCatalogFault fault) {
        ErrorDetail d = fault.getFaultInfo();
        String code = d == null ? "UNKNOWN" : d.getCode();
        String message = d == null ? fault.getMessage() : d.getMessage();
        Kind kind = switch (code) {
            case "BOOK_NOT_FOUND" -> Kind.NOT_FOUND;
            case "INVALID_ISBN", "INVALID_REQUEST" -> Kind.INVALID_REQUEST;
            default -> Kind.UPSTREAM_ERROR;
        };
        return new LegacyCatalogException(kind, code, message, fault);
    }

    /** TH1-7: fault không có mô hình — chỉ còn faultcode soap:Client / soap:Server. */
    static LegacyCatalogException translate(SOAPFaultException e) {
        SOAPFault f = e.getFault();
        String faultCode = f.getFaultCodeAsQName().getLocalPart();
        Kind kind = "Client".equals(faultCode) ? Kind.INVALID_REQUEST : Kind.UPSTREAM_ERROR;
        return new LegacyCatalogException(kind, "soap:" + faultCode, f.getFaultString(), e);
    }

    static LegacyCatalogException unavailable(WebServiceException e) {
        boolean timeout = hasCause(e, SocketTimeoutException.class)
                || hasCause(e, java.net.http.HttpTimeoutException.class);
        return new LegacyCatalogException(timeout ? Kind.UPSTREAM_TIMEOUT : Kind.UPSTREAM_UNAVAILABLE,
                null, "Không gọi được BookCatalogService: " + e.getMessage(), e);
    }

    private static boolean hasCause(Throwable t, Class<? extends Throwable> type) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (type.isInstance(c)) {
                return true;
            }
        }
        return false;
    }

    /** Ánh xạ kiểu SOAP → mô hình nội bộ (không để lớp sinh từ WSDL "rò" ra API REST). */
    static LegacyBook toModel(Book b) {
        Integer authorId = b.getAuthorId() == null ? null : b.getAuthorId().intValue();
        Integer year = b.getPublishedYear() == null ? null : b.getPublishedYear().getYear();
        return new LegacyBook(b.getIsbn(), b.getTitle(), b.getAuthor(), authorId, year,
                b.getPrice().getAmount(), b.getPrice().getCurrency().value());
    }
}
