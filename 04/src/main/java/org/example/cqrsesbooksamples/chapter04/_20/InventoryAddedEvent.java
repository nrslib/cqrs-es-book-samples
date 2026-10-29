package org.example.cqrsesbooksamples.chapter04._20;

public record InventoryAddedEvent(
    String productId,
    int quantity
) implements InventoryEvent {}
