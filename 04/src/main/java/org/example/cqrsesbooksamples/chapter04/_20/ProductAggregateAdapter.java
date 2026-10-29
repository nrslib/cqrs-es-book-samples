// 紙面照合: 4.19
package org.example.cqrsesbooksamples.chapter04._20;

import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;


@Aggregate
public class ProductAggregateAdapter {
    @AggregateIdentifier
    private String productId() {
        return product == null ? null : product.id();
    }
    private Product product;

    public ProductAggregateAdapter() {
    }

    @CommandHandler
    public ProductAggregateAdapter(CreateProductCommand command) {
        var productId = UUID.randomUUID().toString();
        var event = Product.create(productId, command.name(), command.price());
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(ProductEvent event) {
        if (product != null) {
            product = product.applyEvent(event);
        } else {
            if (event instanceof ProductCreatedEvent e) {
                product = Product.applyEvent(e);
            } else {
                throw new IllegalStateException("Expected ProductCreatedEvent");
            }
        }
    }
}
