package org.example.cqrsesbooksamples.chapter04._37;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.example.cqrsesbooksamples.chapter04._34.AccountId;
import org.example.cqrsesbooksamples.chapter04._34.Money;
import org.junit.jupiter.api.Test;

class TransferTest {
    @Test
    void 送金はイベントの適用で状態遷移する() {
        var registered = Transfer.register(
            new TransferId("t-1"), new AccountId("A"), new AccountId("B"),
            new Money(new BigDecimal("100")));

        var transfer = new Transfer(null, null, null, null, null).applyEvent(registered);
        assertThat(transfer.status()).isEqualTo(TransferStatus.PROCESSING);

        var completed = transfer.applyEvent(transfer.complete());
        assertThat(completed.status()).isEqualTo(TransferStatus.COMPLETED);
    }

    @Test
    void 処理中でない送金は完了できない() {
        var registered = Transfer.register(
            new TransferId("t-1"), new AccountId("A"), new AccountId("B"),
            new Money(new BigDecimal("100")));
        var transfer = new Transfer(null, null, null, null, null).applyEvent(registered);
        var failed = transfer.applyEvent(transfer.fail("入金失敗"));

        assertThatThrownBy(failed::complete)
            .isInstanceOf(IllegalStateException.class);
    }
}
