package org.example.cqrsesbooksamples.chapter04._37.events;

import org.example.cqrsesbooksamples.chapter04._34.AccountId;
import org.example.cqrsesbooksamples.chapter04._37.TransferId;

public record AccountCreditFailedEvent(AccountId accountId, TransferId transferId, String reason) implements AccountEvent {}
