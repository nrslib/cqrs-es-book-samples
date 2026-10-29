package org.example.cqrsesbooksamples.chapter03._03;

import java.math.BigDecimal;

public record CreateProductCommand(
    String productId,
    String name,
    BigDecimal price
) {}
