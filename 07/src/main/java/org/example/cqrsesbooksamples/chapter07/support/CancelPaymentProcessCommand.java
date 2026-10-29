package org.example.cqrsesbooksamples.chapter07.support;

public record CancelPaymentProcessCommand(PaymentProcessId paymentProcessId, String reason) {
}
