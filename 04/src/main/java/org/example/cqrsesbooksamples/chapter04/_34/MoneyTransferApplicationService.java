// 紙面照合: 4.31
package org.example.cqrsesbooksamples.chapter04._34;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// アプリケーションサービス
@Service
public class MoneyTransferApplicationService {
    private final AccountRepository accountRepository;
    private final MoneyTransferService transferService = new MoneyTransferService();

    public MoneyTransferApplicationService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void transfer(AccountId from, AccountId to, Money amount) {
        Account source = accountRepository.findById(from)
            .orElseThrow(() -> new AccountNotFoundException(from));
        Account target = accountRepository.findById(to)
            .orElseThrow(() -> new AccountNotFoundException(to));

        transferService.transfer(source, target, amount);

        accountRepository.save(source);
        accountRepository.save(target);
    }
}
