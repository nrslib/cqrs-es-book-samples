// 紙面照合: 4.34
package org.example.cqrsesbooksamples.chapter04._39;

import org.example.cqrsesbooksamples.chapter04._34.AccountId;
import org.example.cqrsesbooksamples.chapter04._34.Money;
import org.example.cqrsesbooksamples.chapter04._37.TransferId;
import org.example.cqrsesbooksamples.chapter04._37.events.AccountCreditedEvent;
import org.example.cqrsesbooksamples.chapter04._37.events.AccountDebitFailedEvent;
import org.example.cqrsesbooksamples.chapter04._37.events.AccountEvent;
import org.example.cqrsesbooksamples.chapter04._37.events.AccountDebitedEvent;
import org.example.cqrsesbooksamples.chapter04._37.events.AccountRefundedEvent;


// イベントソーシング版の口座集約
public class Account {
    private final AccountId id;
    private Money balance;

    public Account(AccountId id, Money balance) {
        this.id = id;
        this.balance = balance;
    }

    public AccountEvent debit(Money amount, TransferId transferId) {
        if (balance.isLessThan(amount)) {
            return new AccountDebitFailedEvent(id, transferId, "残高不足");
        }
        return new AccountDebitedEvent(id, amount, transferId);
    }

    public AccountEvent credit(Money amount, TransferId transferId) {
        return new AccountCreditedEvent(id, amount, transferId);
    }

    public AccountEvent refund(Money amount, TransferId transferId) {
        return new AccountRefundedEvent(id, amount, transferId);
    }

    public Account applyEvent(AccountEvent event) {
        return switch (event) {
            case AccountDebitedEvent e ->
                new Account(id, balance.subtract(e.amount()));
            case AccountCreditedEvent e ->
                new Account(id, balance.add(e.amount()));
            case AccountRefundedEvent e ->
                new Account(id, balance.add(e.amount()));
            case AccountDebitFailedEvent e -> this;  // 失敗は記録するだけで残高は変えない
            default -> throw new IllegalStateException("Unknown event: " + event);
        };
    }

    public Money balance() {
        return balance;
    }
}
