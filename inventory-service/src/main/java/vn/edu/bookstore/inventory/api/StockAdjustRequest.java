package vn.edu.bookstore.inventory.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Điều chỉnh số lượng có sẵn (nghiệp vụ của nhân viên kho). */
public record StockAdjustRequest(@NotNull @Min(0) @Max(100000) Integer available) {
}
