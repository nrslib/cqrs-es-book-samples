package org.example.cqrsesbooksamples.chapter07.support;

import java.math.BigDecimal;

public record RequestBankTransferCommand(PaymentProcessId paymentProcessId, AccountId accountId, BigDecimal amount) {
}
