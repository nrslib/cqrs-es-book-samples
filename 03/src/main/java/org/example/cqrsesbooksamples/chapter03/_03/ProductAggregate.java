package org.example.cqrsesbooksamples.chapter03._03;

import java.math.BigDecimal;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate(snapshotTriggerDefinition = "customSnapshotTrigger")
public class ProductAggregate {
    @AggregateIdentifier
    private String productId;
    private BigDecimal price;
    private ProductStatus status;

    public ProductAggregate() {
    }

    @CommandHandler
    public ProductAggregate(CreateProductCommand command) {
        AggregateLifecycle.apply(new ProductCreatedEvent(
            command.productId(), command.name(), command.price()));
    }

    @CommandHandler
    public void handle(ChangePriceCommand command) {
        AggregateLifecycle.apply(new PriceChangedEvent(command.productId(), command.newPrice()));
    }

    @CommandHandler
    public void handle(ChangeStatusCommand command) {
        AggregateLifecycle.apply(new StatusChangedEvent(command.productId(), command.newStatus()));
    }

    @EventSourcingHandler
    public void on(ProductCreatedEvent event) {
        this.productId = event.productId();
        this.price = event.price();
        this.status = ProductStatus.AVAILABLE;
    }

    @EventSourcingHandler
    public void on(PriceChangedEvent event) {
        this.price = event.newPrice();
    }

    @EventSourcingHandler
    public void on(StatusChangedEvent event) {
        this.status = event.newStatus();
    }
}
