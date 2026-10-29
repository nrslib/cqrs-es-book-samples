package org.example.cqrsesbooksamples.chapter04._34;

import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findById(AccountId id);

    Account save(Account account);
}
