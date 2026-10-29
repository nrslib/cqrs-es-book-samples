package org.example.cqrsesbooksamples.chapter06._02;

import java.math.BigDecimal;
import java.util.UUID;

import org.example.cqrsesbooksamples.chapter06._01.PaymentMethod;

// 削除済み（旧 6.2）
public record RequestPaymentCommand(
    UUID accountId,
    BigDecimal amount,
    PaymentMethod paymentMethod
) {}
