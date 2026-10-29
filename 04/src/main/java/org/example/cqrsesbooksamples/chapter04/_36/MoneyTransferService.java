package org.example.cqrsesbooksamples.chapter04._36;

import org.example.cqrsesbooksamples.chapter04._34.Account;
import org.example.cqrsesbooksamples.chapter04._34.Money;

// 削除済み（旧 4.36）
// リポジトリを使用しないドメインサービス
public class MoneyTransferService {

    public void transfer(Account source, Account target, Money amount) {
        // ドメインロジックのみを担当
        source.withdraw(amount);
        target.deposit(amount);
    }
}
