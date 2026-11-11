package vn.edu.bookstore.order.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vn.edu.bookstore.order.service.InvalidOrderException;
import vn.edu.bookstore.order.service.OrderService;
import vn.edu.bookstore.order.service.PlaceOrderResult;

/** /api/v1/orders — hợp đồng: src/main/resources/static/openapi.yaml */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrderView> create(@RequestHeader(value = "Idempotency-Key", required = false) String key,
                                            @Valid @RequestBody CreateOrderRequest request) {
        if (key != null && (key.isBlank() || key.length() > 100)) {
            throw new InvalidOrderException("Idempotency-Key phải có 1..100 ký tự");
        }
        long customerId = request.customerId();
        PlaceOrderResult result = service.place(customerId, request, key);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(result.order().id()).toUri();
        return ResponseEntity.created(location)
                .header("Idempotent-Replayed", String.valueOf(result.replayed()))
                .body(result.order());
    }

    @GetMapping("/{id}")
    public OrderView get(@PathVariable long id) {
        return service.get(id);
    }

    @GetMapping
    public List<OrderView> list(@RequestParam(name = "customerId", required = false) Long customerId) {
        if (customerId == null) {
            throw new InvalidOrderException("Cần tham số customerId");
        }
        return service.listByCustomer(customerId);
    }
}
