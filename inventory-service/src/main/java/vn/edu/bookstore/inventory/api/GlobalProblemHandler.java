package vn.edu.bookstore.inventory.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalProblemHandler extends ResponseEntityExceptionHandler {

    static final URI VALIDATION_FAILED = URI.create("https://api.bookstore.vn/problems/validation-failed");

    @ExceptionHandler(StockNotFoundException.class)
    ProblemDetail notFound(StockNotFoundException e, HttpServletRequest req) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        p.setTitle("Không có dữ liệu tồn kho");
        p.setInstance(URI.create(req.getRequestURI()));
        return p;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail invalid(ConstraintViolationException e, HttpServletRequest req) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        p.setType(VALIDATION_FAILED);
        p.setTitle("Dữ liệu không hợp lệ");
        p.setInstance(URI.create(req.getRequestURI()));
        return p;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Thân yêu cầu không hợp lệ");
        p.setType(VALIDATION_FAILED);
        p.setTitle("Dữ liệu không hợp lệ");
        return ResponseEntity.badRequest().body(p);
    }
}
