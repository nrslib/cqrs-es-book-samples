package org.example.cqrsesbooksamples.chapter06._01;

import java.math.BigDecimal;

public record CreditMakePaymentRequest(
    PaymentProcessId paymentProcessId,
    AccountId accountId,
    BigDecimal amount
) {}
