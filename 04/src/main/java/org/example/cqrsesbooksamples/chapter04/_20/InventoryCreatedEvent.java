package org.example.cqrsesbooksamples.chapter04._20;

public record InventoryCreatedEvent(
    String productId,
    int quantity
) implements InventoryEvent {}
