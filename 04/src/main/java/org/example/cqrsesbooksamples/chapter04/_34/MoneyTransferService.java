// 紙面照合: 4.31
package org.example.cqrsesbooksamples.chapter04._34;

// ドメインサービス
public class MoneyTransferService {
    public void transfer(Account source, Account target, Money amount) {
        if (source.getId().equals(target.getId())) {
            throw new IllegalArgumentException("送金元と送金先には別の口座を指定してください");
        }
        source.withdraw(amount);
        target.deposit(amount);
    }
}
