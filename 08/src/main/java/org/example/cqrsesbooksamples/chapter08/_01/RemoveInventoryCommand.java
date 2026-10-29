package org.example.cqrsesbooksamples.chapter08._01;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record RemoveInventoryCommand(@TargetAggregateIdentifier String productId, int quantity) {}
