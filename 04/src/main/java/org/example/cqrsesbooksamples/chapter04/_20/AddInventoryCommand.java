package org.example.cqrsesbooksamples.chapter04._20;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record AddInventoryCommand(
    String productId,
    int quantity
) {
    @TargetAggregateIdentifier
    public InventoryId inventoryId() {
        return new InventoryId(productId);
    }
}
