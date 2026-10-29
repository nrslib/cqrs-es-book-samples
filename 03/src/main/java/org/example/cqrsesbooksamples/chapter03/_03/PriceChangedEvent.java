package org.example.cqrsesbooksamples.chapter03._03;

import java.math.BigDecimal;

public record PriceChangedEvent(
    String productId,
    BigDecimal newPrice
) {}
