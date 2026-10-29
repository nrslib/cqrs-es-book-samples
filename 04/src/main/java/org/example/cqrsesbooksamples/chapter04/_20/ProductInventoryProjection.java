// 紙面照合: 4.22
package org.example.cqrsesbooksamples.chapter04._20;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;


@Component
@ProcessingGroup("product-inventory")
public class ProductInventoryProjection {
    private final ProductInventoryViewRepository repository;

    public ProductInventoryProjection(ProductInventoryViewRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(ProductCreatedEvent event) {
        var view = new ProductInventoryView(
            event.productId(),
            event.name(),
            event.price(),
            0
        );
        repository.save(view);
    }

    @EventHandler
    public void on(InventoryCreatedEvent event) {
        var view = repository.findById(event.productId())
            .orElseThrow();
        view.setQuantity(event.quantity());
        repository.save(view);
    }

    @EventHandler
    public void on(InventoryAddedEvent event) {
        var view = repository.findById(event.productId())
            .orElseThrow();
        view.setQuantity(view.getQuantity() + event.quantity());
        repository.save(view);
    }
}
