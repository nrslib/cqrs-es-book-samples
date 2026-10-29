package org.example.cqrsesbooksamples.chapter04._20;

import java.math.BigDecimal;

public record CreateProductCommand(
    String name,
    BigDecimal price
) {}
