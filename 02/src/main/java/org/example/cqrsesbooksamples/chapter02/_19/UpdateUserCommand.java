package org.example.cqrsesbooksamples.chapter02._19;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record UpdateUserCommand(
    @TargetAggregateIdentifier String userId,
    String name,
    String email
) {}
