// 紙面照合: 6.4
package org.example.cqrsesbooksamples.chapter06._01;

import java.math.BigDecimal;


public record PaymentProcessAcceptedEvent(
    PaymentProcessId paymentProcessId,
    AccountId accountId,
    BigDecimal amount,
    PaymentMethod paymentMethod
) implements PaymentProcessEvent {}
