package org.example.cqrsesbooksamples.chapter03._11;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record PingCommand(
    @TargetAggregateIdentifier String id
) {}
