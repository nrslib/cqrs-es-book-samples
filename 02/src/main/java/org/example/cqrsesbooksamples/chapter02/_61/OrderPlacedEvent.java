package org.example.cqrsesbooksamples.chapter02._61;

public record OrderPlacedEvent(
    String orderId,
    int amount
) {}
