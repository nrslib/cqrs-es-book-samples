package org.example.cqrsesbooksamples.chapter07._19;

import java.math.BigDecimal;

public record PaymentRequest(String orderId, BigDecimal amount, String paymentMethod) {}
