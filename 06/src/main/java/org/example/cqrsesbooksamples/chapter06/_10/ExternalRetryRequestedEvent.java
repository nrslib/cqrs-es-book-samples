package org.example.cqrsesbooksamples.chapter06._10;

import org.example.cqrsesbooksamples.chapter06._01.ExecuteCreditPaymentCommand;

public record ExternalRetryRequestedEvent(ExecuteCreditPaymentCommand command) {}
