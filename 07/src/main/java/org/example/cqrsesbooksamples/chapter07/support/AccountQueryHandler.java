package org.example.cqrsesbooksamples.chapter07.support;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class AccountQueryHandler {
    private final Map<UUID, AccountData> accounts = new ConcurrentHashMap<>();

    public void register(AccountData account) {
        accounts.put(account.accountId(), account);
    }

    @QueryHandler
    public GetAccountQueryResult handle(GetAccountQuery query) {
        return new GetAccountQueryResult(Optional.ofNullable(accounts.get(query.accountId())));
    }
}
