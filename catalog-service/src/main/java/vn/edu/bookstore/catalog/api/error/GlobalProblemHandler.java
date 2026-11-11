package vn.edu.bookstore.catalog.api.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import vn.edu.bookstore.catalog.service.BookNotFoundException;
import vn.edu.bookstore.catalog.service.DuplicateIsbnException;
import vn.edu.bookstore.catalog.service.PreconditionFailedException;
import vn.edu.bookstore.catalog.service.PreconditionRequiredException;

/** TH2-6: mọi lỗi → application/problem+json (RFC 9457). */
@RestControllerAdvice
public class GlobalProblemHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    ProblemDetail notFound(BookNotFoundException e, HttpServletRequest req) {
        return problem(HttpStatus.NOT_FOUND, ProblemTypes.BOOK_NOT_FOUND, "Không tìm thấy sách", e.getMessage(), req);
    }

    @ExceptionHandler(DuplicateIsbnException.class)
    ProblemDetail duplicate(DuplicateIsbnException e, HttpServletRequest req) {
        return problem(HttpStatus.CONFLICT, null, "ISBN đã tồn tại", e.getMessage(), req);
    }

    @ExceptionHandler(PreconditionFailedException.class)
    ProblemDetail preconditionFailed(PreconditionFailedException e, HttpServletRequest req) {
        return problem(HttpStatus.PRECONDITION_FAILED, null, "Phiên bản đã thay đổi", e.getMessage(), req);
    }

    @ExceptionHandler(PreconditionRequiredException.class)
    ProblemDetail preconditionRequired(PreconditionRequiredException e, HttpServletRequest req) {
        return problem(HttpStatus.PRECONDITION_REQUIRED, null, "Thiếu If-Match", e.getMessage(), req);
    }

    /** Vi phạm ràng buộc trên tham số đường dẫn/truy vấn (interface sinh ra có @Validated). */
    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail constraint(ConstraintViolationException e, HttpServletRequest req) {
        ProblemDetail p = problem(HttpStatus.BAD_REQUEST, ProblemTypes.VALIDATION_FAILED,
                "Dữ liệu không hợp lệ", "Tham số không khớp hợp đồng API", req);
        p.setProperty("errors", e.getConstraintViolations().stream()
                .map(v -> Map.of("field", lastNode(v.getPropertyPath().toString()), "message", v.getMessage()))
                .toList());
        return p;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail p = problem(HttpStatus.BAD_REQUEST, ProblemTypes.VALIDATION_FAILED,
                "Dữ liệu không hợp lệ", "Thân yêu cầu không khớp hợp đồng API", path(request));
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> Map.of("field", f.getField(), "message", String.valueOf(f.getDefaultMessage())))
                .toList();
        p.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(p);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail p = problem(HttpStatus.BAD_REQUEST, ProblemTypes.VALIDATION_FAILED,
                "Dữ liệu không hợp lệ", "Tham số không khớp hợp đồng API", path(request));
        p.setProperty("errors", ex.getAllErrors().stream()
                .map(err -> Map.of("message", String.valueOf(err.getDefaultMessage()))).toList());
        return ResponseEntity.badRequest().body(p);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail p = problem(HttpStatus.BAD_REQUEST, ProblemTypes.VALIDATION_FAILED,
                "Dữ liệu không hợp lệ", "Không đọc được thân yêu cầu JSON", path(request));
        return ResponseEntity.badRequest().body(p);
    }

    private static String lastNode(String path) {
        int i = path.lastIndexOf('.');
        return i < 0 ? path : path.substring(i + 1);
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
