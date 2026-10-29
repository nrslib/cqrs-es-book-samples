package org.example.cqrsesbooksamples.chapter06._01;

public record CreditMakePaymentFailedEvent(ExecuteCreditPaymentCommand command, String message) {}
