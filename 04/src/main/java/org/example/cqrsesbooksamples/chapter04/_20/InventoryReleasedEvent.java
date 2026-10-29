package org.example.cqrsesbooksamples.chapter04._20;

public record InventoryReleasedEvent(
    String productId,
    int quantity,
    String orderId
) implements InventoryEvent {}
