package vn.edu.bookstore.order.api;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import vn.edu.bookstore.order.client.CatalogUnavailableException;
import vn.edu.bookstore.order.client.UnknownBookException;
import vn.edu.bookstore.order.service.IdempotencyConflictException;
import vn.edu.bookstore.order.service.InvalidOrderException;
import vn.edu.bookstore.order.service.OrderNotFoundException;
@RestControllerAdvice
public class GlobalProblemHandler extends ResponseEntityExceptionHandler {

    private static final String BASE = "https://api.bookstore.vn/problems/";
    static final URI VALIDATION_FAILED = URI.create(BASE + "validation-failed");
    static final URI UPSTREAM_UNAVAILABLE = URI.create(BASE + "upstream-unavailable");
    static final URI INSUFFICIENT_STOCK = URI.create(BASE + "insufficient-stock");
    static final URI FORBIDDEN = URI.create(BASE + "forbidden");

    @ExceptionHandler(UnknownBookException.class)
    ProblemDetail unknownBook(UnknownBookException e, HttpServletRequest req) {
        ProblemDetail p = problem(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, "Sách không tồn tại", e.getMessage(), req);
        p.setProperty("isbn", e.isbn());
        return p;
    }

    /** TH3-5: catalog chậm/tắt/cầu dao mở → 503 + Retry-After (trả lỗi nhanh, không treo luồng). */
    @ExceptionHandler(CatalogUnavailableException.class)
    ResponseEntity<ProblemDetail> upstream(CatalogUnavailableException e, HttpServletRequest req) {
        ProblemDetail p = problem(HttpStatus.SERVICE_UNAVAILABLE, UPSTREAM_UNAVAILABLE,
                "Dịch vụ danh mục tạm thời không sẵn sàng", e.getMessage(), req);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).header(HttpHeaders.RETRY_AFTER, "10").body(p);
    }

    @ExceptionHandler(InvalidOrderException.class)
    ProblemDetail invalid(InvalidOrderException e, HttpServletRequest req) {
        return problem(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, "Đơn hàng không hợp lệ", e.getMessage(), req);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail notFound(OrderNotFoundException e, HttpServletRequest req) {
        return problem(HttpStatus.NOT_FOUND, null, "Không tìm thấy đơn hàng", e.getMessage(), req);
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    ProblemDetail idempotency(IdempotencyConflictException e, HttpServletRequest req) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, null, "Xung đột Idempotency-Key", e.getMessage(), req);
    }
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail p = problem(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, "Dữ liệu không hợp lệ",
                "Thân yêu cầu không khớp hợp đồng API", path(request));
        p.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .map(f -> java.util.Map.of("field", f.getField(), "message", String.valueOf(f.getDefaultMessage())))
                .toList());
        return ResponseEntity.badRequest().body(p);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest().body(problem(HttpStatus.BAD_REQUEST, VALIDATION_FAILED,
                "Dữ liệu không hợp lệ", "Không đọc được thân yêu cầu JSON", path(request)));
    }

    private static String path(WebRequest request) {
        return request instanceof ServletWebRequest s ? s.getRequest().getRequestURI() : null;
    }

    static ProblemDetail problem(HttpStatus status, URI type, String title, String detail, HttpServletRequest req) {
        return problem(status, type, title, detail, req.getRequestURI());
    }

    static ProblemDetail problem(HttpStatus status, URI type, String title, String detail, String path) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detail);
        if (type != null) {
            p.setType(type);
        }
        p.setTitle(title);
        if (path != null) {
            p.setInstance(URI.create(path));
        }
        return p;
    }
}
