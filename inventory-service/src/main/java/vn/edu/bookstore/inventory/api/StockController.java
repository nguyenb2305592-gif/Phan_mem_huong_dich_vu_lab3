package vn.edu.bookstore.inventory.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.bookstore.inventory.domain.StockItem;
import vn.edu.bookstore.inventory.domain.StockItemRepository;

/** /api/v1/inventory — hợp đồng: src/main/resources/static/openapi.yaml */
@RestController
@RequestMapping("/api/v1/inventory")
@Validated
public class StockController {

    private final StockItemRepository repository;

    public StockController(StockItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{isbn}")
    public StockView get(@PathVariable @Pattern(regexp = "^97[89][0-9]{10}$") String isbn) {
        return repository.findById(isbn).map(StockView::from).orElseThrow(() -> new StockNotFoundException(isbn));
    }

    @PutMapping("/{isbn}")
    @Transactional
    public StockView adjust(@PathVariable @Pattern(regexp = "^97[89][0-9]{10}$") String isbn,
                            @Valid @RequestBody StockAdjustRequest request) {
        StockItem item = repository.findById(isbn).orElseGet(() -> new StockItem(isbn, 0));
        item.setAvailable(request.available());
        return StockView.from(repository.saveAndFlush(item));
    }
}
