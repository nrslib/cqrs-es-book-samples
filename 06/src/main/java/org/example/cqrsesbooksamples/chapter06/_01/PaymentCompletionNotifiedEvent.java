package org.example.cqrsesbooksamples.chapter06._01;

public record PaymentCompletionNotifiedEvent(
    PaymentProcessId paymentProcessId,
    AccountId accountId
) {
}
