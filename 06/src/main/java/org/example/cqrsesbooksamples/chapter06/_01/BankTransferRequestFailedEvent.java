package org.example.cqrsesbooksamples.chapter06._01;

public record BankTransferRequestFailedEvent(
    PaymentProcessId paymentProcessId,
    String reason
) {}
