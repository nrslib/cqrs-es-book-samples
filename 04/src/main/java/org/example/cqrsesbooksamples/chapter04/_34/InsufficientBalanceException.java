package org.example.cqrsesbooksamples.chapter04._34;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(AccountId id, Money balance, Money requested) {
        super("残高不足: account=" + id + ", balance=" + balance.amount() + ", requested=" + requested.amount());
    }
}
