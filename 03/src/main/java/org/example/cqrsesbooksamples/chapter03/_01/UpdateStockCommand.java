package org.example.cqrsesbooksamples.chapter03._01;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record UpdateStockCommand(
    @TargetAggregateIdentifier String productId,
    int quantity
) {}
