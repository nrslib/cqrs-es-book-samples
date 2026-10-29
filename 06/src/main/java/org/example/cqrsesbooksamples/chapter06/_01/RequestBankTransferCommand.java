package org.example.cqrsesbooksamples.chapter06._01;

import java.math.BigDecimal;

// 削除済み（旧 6.20）
public record RequestBankTransferCommand(
    PaymentProcessId paymentProcessId,
    AccountId accountId,
    BigDecimal amount
) {}
