// 紙面照合: 4.20, 4.26
package org.example.cqrsesbooksamples.chapter04._20;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.CreationPolicy;
import org.axonframework.spring.stereotype.Aggregate;


@Aggregate
public class InventoryAggregateAdapter {
    @AggregateIdentifier
    private InventoryId inventoryId() {
        return inventory == null ? null : new InventoryId(inventory.productId());
    }
    private Inventory inventory;

    public InventoryAggregateAdapter() {
    }


    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    public void handle(AddInventoryCommand command) {
        var event = inventory == null
            ? Inventory.create(command.productId(), command.quantity())
            : inventory.add(command.quantity());
        AggregateLifecycle.apply(event);
    }


    @CommandHandler
    public void handle(ReserveInventoryCommand command) {
        AggregateLifecycle.apply(
            inventory.reserve(command.quantity(), command.orderId()));
    }

    @CommandHandler
    public void handle(ReleaseInventoryCommand command) {
        inventory.release(command.orderId())
            .ifPresent(AggregateLifecycle::apply);
    }

    @EventSourcingHandler
    public void on(InventoryEvent event) {
        if (inventory != null) {
            inventory = inventory.applyEvent(event);
        } else {
            if (event instanceof InventoryCreatedEvent e) {
                inventory = Inventory.applyEvent(e);
            } else {
                throw new IllegalStateException("Expected InventoryCreatedEvent");
            }
        }
    }
}
