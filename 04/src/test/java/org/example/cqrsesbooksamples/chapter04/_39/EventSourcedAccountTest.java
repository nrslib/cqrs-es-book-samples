package org.example.cqrsesbooksamples.chapter04._39;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.example.cqrsesbooksamples.chapter04._34.AccountId;
import org.example.cqrsesbooksamples.chapter04._37.events.AccountDebitFailedEvent;
import org.example.cqrsesbooksamples.chapter04._34.Money;
import org.example.cqrsesbooksamples.chapter04._37.TransferId;
import org.junit.jupiter.api.Test;

class EventSourcedAccountTest {
    @Test
    void 引き落としと入金はイベントとして返され適用で残高が変わる() {
        var account = new Account(new AccountId("A"), money("1000"));
        var transferId = new TransferId("t-1");

        var debited = account.debit(money("300"), transferId);
        account = account.applyEvent(debited);
        assertThat(account.balance()).isEqualTo(money("700"));

        var refunded = account.refund(money("300"), transferId);
        account = account.applyEvent(refunded);
        assertThat(account.balance()).isEqualTo(money("1000"));
    }

    @Test
    void 残高を超える引き落としは失敗イベントとして返り残高は変わらない() {
        var account = new Account(new AccountId("A"), money("100"));

        var failed = account.debit(money("300"), new TransferId("t-1"));

        assertThat(failed).isInstanceOf(AccountDebitFailedEvent.class);
        assertThat(account.applyEvent(failed).balance()).isEqualTo(money("100"));
    }

    private static Money money(String amount) {
        return new Money(new BigDecimal(amount));
    }
}
