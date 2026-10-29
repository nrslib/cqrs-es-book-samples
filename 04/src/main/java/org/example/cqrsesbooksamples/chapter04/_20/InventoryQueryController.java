package org.example.cqrsesbooksamples.chapter04._20;

import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 削除済み（旧 4.26）
@RestController
@RequestMapping("/inventory")
public class InventoryQueryController {
    private final QueryGateway queryGateway;

    public InventoryQueryController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @GetMapping("/{productId}")
    public ProductInventoryResponse getInventory(@PathVariable String productId) {
        var query = new GetProductInventoryQuery(productId);
        var result = queryGateway.query(query, ProductInventoryQueryResult.class).join();
        return new ProductInventoryResponse(
            result.name(),
            result.price(),
            result.quantity()
        );
    }
}
