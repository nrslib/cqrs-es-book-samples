package org.example.cqrsesbooksamples.chapter04._29;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record CompleteOrderCommand(
    @TargetAggregateIdentifier String orderId
) {}
