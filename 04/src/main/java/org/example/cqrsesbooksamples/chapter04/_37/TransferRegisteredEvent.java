package org.example.cqrsesbooksamples.chapter04._37;

import org.example.cqrsesbooksamples.chapter04._34.AccountId;
import org.example.cqrsesbooksamples.chapter04._34.Money;

public record TransferRegisteredEvent(
    TransferId transferId,
    AccountId sourceAccountId,
    AccountId targetAccountId,
    Money amount
) implements TransferEvent {}
