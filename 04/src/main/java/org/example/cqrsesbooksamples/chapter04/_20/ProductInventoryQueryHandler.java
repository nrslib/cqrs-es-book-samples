package org.example.cqrsesbooksamples.chapter04._20;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class ProductInventoryQueryHandler {
    private final ProductInventoryViewRepository repository;

    public ProductInventoryQueryHandler(ProductInventoryViewRepository repository) {
        this.repository = repository;
    }

    @QueryHandler
    public ProductInventoryQueryResult handle(GetProductInventoryQuery query) {
        var view = repository.findById(query.productId()).orElseThrow();
        return new ProductInventoryQueryResult(view.getName(), view.getPrice(), view.getQuantity());
    }

    @QueryHandler
    public InventoryQueryResult handle(GetInventoryQuery query) {
        var view = repository.findById(query.productId()).orElseThrow();
        return new InventoryQueryResult(view.getQuantity());
    }
}
