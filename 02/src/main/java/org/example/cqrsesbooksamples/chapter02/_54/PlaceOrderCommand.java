package org.example.cqrsesbooksamples.chapter02._54;

public record PlaceOrderCommand(
    String orderId,
    int amount
) {}
