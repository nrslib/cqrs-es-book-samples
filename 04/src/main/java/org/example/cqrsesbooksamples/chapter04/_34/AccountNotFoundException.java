package org.example.cqrsesbooksamples.chapter04._34;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(AccountId id) {
        super("口座が見つかりません: " + id);
    }
}
