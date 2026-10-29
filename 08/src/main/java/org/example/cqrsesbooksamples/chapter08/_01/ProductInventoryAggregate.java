package org.example.cqrsesbooksamples.chapter08._01;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class ProductInventoryAggregate {
    @AggregateIdentifier
    private String productId;
    private int quantity;

    public ProductInventoryAggregate() {
    }

    @CommandHandler
    public ProductInventoryAggregate(AddInventoryCommand command) {
        if (command.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        AggregateLifecycle.apply(new InventoryAddedEvent(command.productId(), command.quantity()));
    }

    @CommandHandler
    public void handle(RemoveInventoryCommand command) {
        if (command.quantity() < 0) {
            throw new IllegalArgumentException("Removal quantity must not be negative");
        }
        if (command.quantity() > quantity) {
            throw new IllegalStateException("Insufficient inventory");
        }
        AggregateLifecycle.apply(new InventoryRemovedEvent(command.productId(), command.quantity()));
    }

    @EventSourcingHandler
    public void on(InventoryAddedEvent event) {
        this.productId = event.productId();
        this.quantity += event.quantity();
    }

    @EventSourcingHandler
    public void on(InventoryRemovedEvent event) {
        this.quantity -= event.quantity();
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}
