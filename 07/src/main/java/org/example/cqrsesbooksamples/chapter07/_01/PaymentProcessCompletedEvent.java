package org.example.cqrsesbooksamples.chapter07._01;

import org.example.cqrsesbooksamples.chapter07.support.PaymentProcessId;

public record PaymentProcessCompletedEvent(PaymentProcessId paymentProcessId) implements PaymentProcessEvent {}
