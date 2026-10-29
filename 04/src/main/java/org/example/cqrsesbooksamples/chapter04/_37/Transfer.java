// 紙面照合: 4.32
package org.example.cqrsesbooksamples.chapter04._37;

import org.example.cqrsesbooksamples.chapter04._34.AccountId;
import org.example.cqrsesbooksamples.chapter04._34.Money;


// Transfer集約
public class Transfer {
    private final TransferId id;
    private final AccountId sourceAccountId;
    private final AccountId targetAccountId;
    private final Money amount;
    private TransferStatus status;

    public Transfer(TransferId id, AccountId sourceAccountId, AccountId targetAccountId,
                    Money amount, TransferStatus status) {
        this.id = id;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
        this.status = status;
    }

    // 送金を事実として登録する
    public static TransferRegisteredEvent register(
            TransferId id,
            AccountId sourceAccountId,
            AccountId targetAccountId,
            Money amount) {

        return new TransferRegisteredEvent(
            id, sourceAccountId, targetAccountId, amount);
    }

    public TransferCompletedEvent complete() {
        if (status != TransferStatus.PROCESSING) {
            throw new IllegalStateException("処理中の送金のみ完了できます");
        }
        return new TransferCompletedEvent(id);
    }

    public TransferFailedEvent fail(String reason) {
        if (status != TransferStatus.PROCESSING) {
            throw new IllegalStateException("処理中の送金のみ失敗にできます");
        }
        return new TransferFailedEvent(id, reason);
    }

    // イベントを適用して状態を復元
    public Transfer applyEvent(TransferEvent event) {
        return switch (event) {
            case TransferRegisteredEvent e -> new Transfer(
                e.transferId(), e.sourceAccountId(), e.targetAccountId(),
                e.amount(), TransferStatus.PROCESSING);
            case TransferCompletedEvent e -> withStatus(TransferStatus.COMPLETED);
            case TransferFailedEvent e -> withStatus(TransferStatus.FAILED);
            default -> throw new IllegalStateException("Unknown event: " + event);
        };
    }

    private Transfer withStatus(TransferStatus newStatus) {
        return new Transfer(id, sourceAccountId, targetAccountId, amount, newStatus);
    }

    public TransferStatus status() {
        return status;
    }
}
