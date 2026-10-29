package org.example.cqrsesbooksamples.chapter03._01;

import java.math.BigDecimal;

public record ProductCreatedEvent(
    String productId,
    String name,
    BigDecimal price,
    int initialStock
) {}
