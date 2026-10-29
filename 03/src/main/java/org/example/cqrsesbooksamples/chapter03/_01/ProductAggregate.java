// 紙面照合: 3.1
package org.example.cqrsesbooksamples.chapter03._01;

import java.math.BigDecimal;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

// 削除済み（旧 3.1）
@Aggregate(snapshotTriggerDefinition = "productSnapshotTrigger")
public class ProductAggregate {
    @AggregateIdentifier
    private String productId;
    private String name;
    private BigDecimal price;
    private int stockQuantity;
    private ProductStatus status;

    public ProductAggregate() {
    }

    @CommandHandler
    public ProductAggregate(CreateProductCommand command) {
        AggregateLifecycle.apply(new ProductCreatedEvent(
            command.productId(), command.name(), command.price(), command.initialStock()));
    }

    @CommandHandler
    public void handle(UpdateStockCommand command) {
        AggregateLifecycle.apply(new StockUpdatedEvent(command.productId(), command.quantity()));
    }

    // イベントソーシングハンドラは通常通り実装
    @EventSourcingHandler
    public void on(ProductCreatedEvent event) {
        this.productId = event.productId();
        this.name = event.name();
        this.price = event.price();
        this.stockQuantity = event.initialStock();
        this.status = ProductStatus.AVAILABLE;
    }

    @EventSourcingHandler
    public void on(StockUpdatedEvent event) {
        this.stockQuantity += event.quantity();
        if (this.stockQuantity <= 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        } else {
            this.status = ProductStatus.AVAILABLE;
        }
    }
}
