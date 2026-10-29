package org.example.cqrsesbooksamples.chapter07._17;

import java.math.BigDecimal;

public record PaymentRequest(String orderId, BigDecimal amount, String paymentMethod) {}
