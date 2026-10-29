package org.example.cqrsesbooksamples.chapter02._17;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

record CreateUserCommand(String name, String email) {}

record UpdateUserCommand(
    @TargetAggregateIdentifier String userId,
    String name,
    String email
) {}
