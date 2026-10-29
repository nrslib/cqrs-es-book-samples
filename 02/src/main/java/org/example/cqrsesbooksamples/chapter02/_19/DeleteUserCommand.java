package org.example.cqrsesbooksamples.chapter02._19;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record DeleteUserCommand(
    @TargetAggregateIdentifier String userId
) {}
