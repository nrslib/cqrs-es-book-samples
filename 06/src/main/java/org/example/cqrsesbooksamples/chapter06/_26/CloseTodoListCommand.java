package org.example.cqrsesbooksamples.chapter06._26;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record CloseTodoListCommand(@TargetAggregateIdentifier String todoListId) {}
