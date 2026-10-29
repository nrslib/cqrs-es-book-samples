package org.example.cqrsesbooksamples.chapter04._20;

public record InventoryReservedEvent(
    String productId,
    int quantity,
    String orderId
) implements InventoryEvent {}
