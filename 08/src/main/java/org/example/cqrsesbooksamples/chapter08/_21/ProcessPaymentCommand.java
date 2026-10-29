package org.example.cqrsesbooksamples.chapter08._21;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record ProcessPaymentCommand(@TargetAggregateIdentifier String orderId, int amount) {}
