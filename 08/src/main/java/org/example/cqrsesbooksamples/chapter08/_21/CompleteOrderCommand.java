package org.example.cqrsesbooksamples.chapter08._21;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record CompleteOrderCommand(@TargetAggregateIdentifier String orderId) {}
