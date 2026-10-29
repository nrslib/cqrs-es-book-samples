package org.example.cqrsesbooksamples.chapter06._01;

import java.math.BigDecimal;

public record BankTransferRequest(AccountId accountId, BigDecimal amount) {}
