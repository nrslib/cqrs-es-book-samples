package org.example.cqrsesbooksamples.chapter04._20;

import java.math.BigDecimal;

public record ProductCreatedEvent(
    String productId,
    String name,
    BigDecimal price
) implements ProductEvent {}
