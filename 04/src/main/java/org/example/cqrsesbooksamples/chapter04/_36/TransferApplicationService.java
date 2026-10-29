package org.example.cqrsesbooksamples.chapter04._36;

import org.example.cqrsesbooksamples.chapter04._34.Account;
import org.example.cqrsesbooksamples.chapter04._34.AccountId;
import org.example.cqrsesbooksamples.chapter04._34.AccountNotFoundException;
import org.example.cqrsesbooksamples.chapter04._34.AccountRepository;
import org.example.cqrsesbooksamples.chapter04._34.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 削除済み（旧 4.36）
// アプリケーション層でリポジトリを使用
@Service
public class TransferApplicationService {
    private final AccountRepository accountRepository;
    private final MoneyTransferService moneyTransferService;

    public TransferApplicationService(AccountRepository accountRepository,
                                      MoneyTransferService moneyTransferService) {
        this.accountRepository = accountRepository;
        this.moneyTransferService = moneyTransferService;
    }

    @Transactional
    public void transfer(AccountId from, AccountId to, Money amount) {
        Account sourceAccount = accountRepository.findById(from)
            .orElseThrow(() -> new AccountNotFoundException(from));
        Account targetAccount = accountRepository.findById(to)
            .orElseThrow(() -> new AccountNotFoundException(to));

        moneyTransferService.transfer(sourceAccount, targetAccount, amount);

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);
    }
}
