package org.example.cqrsesbooksamples.chapter04._37.events;

import org.example.cqrsesbooksamples.chapter04._34.AccountId;
import org.example.cqrsesbooksamples.chapter04._34.Money;
import org.example.cqrsesbooksamples.chapter04._37.TransferId;

public record AccountCreditedEvent(AccountId accountId, Money amount, TransferId transferId) implements AccountEvent {}
