package org.example.cqrsesbooksamples.chapter03._03;

public record StatusChangedEvent(
    String productId,
    ProductStatus newStatus
) {}
