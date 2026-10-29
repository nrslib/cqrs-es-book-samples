package org.example.cqrsesbooksamples.chapter04._20;

import java.math.BigDecimal;

public record ProductInventoryResponse(
    String name,
    BigDecimal price,
    int quantity
) {}
