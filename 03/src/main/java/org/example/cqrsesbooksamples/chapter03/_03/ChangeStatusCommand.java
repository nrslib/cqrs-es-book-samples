package org.example.cqrsesbooksamples.chapter03._03;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record ChangeStatusCommand(
    @TargetAggregateIdentifier String productId,
    ProductStatus newStatus
) {}
