package org.example.cqrsesbooksamples.chapter07._20;

import java.math.BigDecimal;

public record PaymentRequestMessage(String orderId, BigDecimal amount) {}
