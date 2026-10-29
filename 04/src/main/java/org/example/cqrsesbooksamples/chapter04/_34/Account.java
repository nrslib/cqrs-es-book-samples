// 紙面照合: 4.30
package org.example.cqrsesbooksamples.chapter04._34;


// 口座集約
public class Account {
    private final AccountId id;
    private Money balance;

    public Account(AccountId id, Money balance) {
        this.id = id;
        this.balance = balance;
    }

    public void withdraw(Money amount) {
        if (balance.isLessThan(amount)) {
            throw new InsufficientBalanceException(id, balance, amount);
        }
        this.balance = balance.subtract(amount);
    }

    public void deposit(Money amount) {
        this.balance = balance.add(amount);
    }

    public Money getBalance() {
        return balance;
    }

    public AccountId getId() {
        return id;
    }
}
