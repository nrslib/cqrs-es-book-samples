package org.example.cqrsesbooksamples.chapter03._03;

import java.math.BigDecimal;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record ChangePriceCommand(
    @TargetAggregateIdentifier String productId,
    BigDecimal newPrice
) {}
