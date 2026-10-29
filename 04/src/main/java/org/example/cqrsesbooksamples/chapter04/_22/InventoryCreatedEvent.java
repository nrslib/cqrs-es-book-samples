package org.example.cqrsesbooksamples.chapter04._22;

public record InventoryCreatedEvent(
    String productId,
    int quantity
) implements InventoryEvent {}
