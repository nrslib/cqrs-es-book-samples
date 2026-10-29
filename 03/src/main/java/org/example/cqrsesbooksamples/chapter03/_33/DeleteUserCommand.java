package org.example.cqrsesbooksamples.chapter03._33;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record DeleteUserCommand(
    @TargetAggregateIdentifier String userId
) {}
