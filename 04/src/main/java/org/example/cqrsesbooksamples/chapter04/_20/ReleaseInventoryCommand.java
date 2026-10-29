package org.example.cqrsesbooksamples.chapter04._20;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record ReleaseInventoryCommand(
    String productId,
    String orderId
) {
    @TargetAggregateIdentifier
    public InventoryId inventoryId() {
        return new InventoryId(productId);
    }
}
