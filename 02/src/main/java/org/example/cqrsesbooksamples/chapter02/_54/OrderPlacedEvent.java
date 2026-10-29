package org.example.cqrsesbooksamples.chapter02._54;

public record OrderPlacedEvent(
    String orderId,
    int amount
) {}
