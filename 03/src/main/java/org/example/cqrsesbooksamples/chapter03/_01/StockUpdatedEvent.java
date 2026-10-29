package org.example.cqrsesbooksamples.chapter03._01;

public record StockUpdatedEvent(
    String productId,
    int quantity
) {}
