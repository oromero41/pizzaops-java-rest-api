package com.pizzaops.api;

import com.pizzaops.PizzaOrder;
import com.pizzaops.PizzaOperations;
import com.pizzaops.PizzaStatistics;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PizzaApiController {
    private final PizzaOperations service;

    public PizzaApiController(PizzaOperations service) {
        this.service = service;
    }

    @GetMapping("/menu")
    public CatalogResponse menu() {
        return CatalogResponse.fromCatalog();
    }

    @GetMapping("/orders")
    public List<PizzaOrder> orders() {
        return service.getOrders();
    }

    @GetMapping("/orders/{id}")
    public PizzaOrder order(@PathVariable long id) {
        return service.getOrder(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    @PostMapping("/orders")
    public ResponseEntity<PizzaOrder> create(@Valid @RequestBody OrderRequest request) {
        PizzaOrder order = service.createOrder(request.size(), request.base(),
                request.sauce(), request.cheese(), request.toppings());
        return ResponseEntity.created(URI.create("/api/v1/orders/" + order.id())).body(order);
    }

    @GetMapping("/statistics")
    public PizzaStatistics statistics() {
        return service.getStatistics();
    }
}
