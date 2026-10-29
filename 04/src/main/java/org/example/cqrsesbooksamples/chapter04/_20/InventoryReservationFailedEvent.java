package org.example.cqrsesbooksamples.chapter04._20;

public record InventoryReservationFailedEvent(
    String productId,
    String orderId
) implements InventoryEvent {}
