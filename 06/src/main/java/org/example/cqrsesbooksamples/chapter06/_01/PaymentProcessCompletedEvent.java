// 紙面照合: 6.14
package org.example.cqrsesbooksamples.chapter06._01;


public record PaymentProcessCompletedEvent(
    PaymentProcessId paymentProcessId,
    AccountId accountId
) implements PaymentProcessEvent {
}
