package org.example.cqrsesbooksamples.chapter07._01;

import org.example.cqrsesbooksamples.chapter07.support.PaymentProcessId;

public record BankTransferRequestedEvent(PaymentProcessId paymentProcessId) implements PaymentProcessEvent {}
