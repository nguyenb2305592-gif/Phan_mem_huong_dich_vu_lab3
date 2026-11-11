package vn.edu.bookstore.order.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Thân POST /api/v1/orders. TH3: customerId nằm trong thân; TH4 sẽ lấy từ JWT. */
public record CreateOrderRequest(
        @NotNull @Positive Long customerId,
        @NotEmpty @Size(max = 20) List<@Valid Line> lines) {

    public record Line(
            @NotNull @Pattern(regexp = "^97[89][0-9]{10}$") String isbn,
            @NotNull @Min(1) @Max(100) Integer quantity) {
    }
}
