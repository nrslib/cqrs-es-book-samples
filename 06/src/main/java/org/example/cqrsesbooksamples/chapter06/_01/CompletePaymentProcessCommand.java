// 紙面照合: 6.12
package org.example.cqrsesbooksamples.chapter06._01;

import org.axonframework.modelling.command.TargetAggregateIdentifier;


public record CompletePaymentProcessCommand(@TargetAggregateIdentifier PaymentProcessId paymentProcessId) {
}
