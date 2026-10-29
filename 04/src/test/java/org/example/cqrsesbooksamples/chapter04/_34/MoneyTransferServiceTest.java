// 検証補助: 4.30, 4.31
package org.example.cqrsesbooksamples.chapter04._34;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class MoneyTransferServiceTest {
    private final MoneyTransferService service = new MoneyTransferService();

    @Test
    void 送金すると同じ金額を引き落として入金する() {
        var source = new Account(new AccountId("A"), money("1000"));
        var target = new Account(new AccountId("B"), money("500"));

        service.transfer(source, target, money("300"));

        assertThat(source.getBalance()).isEqualTo(money("700"));
        assertThat(target.getBalance()).isEqualTo(money("800"));
    }

    @Test
    void 残高不足なら両口座の残高を変えない() {
        var source = new Account(new AccountId("A"), money("1000"));
        var target = new Account(new AccountId("B"), money("500"));

        assertThatThrownBy(() -> service.transfer(source, target, money("1500")))
            .isInstanceOf(InsufficientBalanceException.class);

        assertThat(source.getBalance()).isEqualTo(money("1000"));
        assertThat(target.getBalance()).isEqualTo(money("500"));
    }

    @Test
    void 同じ口座IDなら別インスタンスでも送金を拒否する() {
        var source = new Account(new AccountId("A"), money("1000"));
        var target = new Account(new AccountId("A"), money("1000"));

        assertThatThrownBy(() -> service.transfer(source, target, money("300")))
            .isInstanceOf(IllegalArgumentException.class);

        assertThat(source.getBalance()).isEqualTo(money("1000"));
        assertThat(target.getBalance()).isEqualTo(money("1000"));
    }

    @Test
    void 金額は負の値を持てない() {
        assertThatThrownBy(() -> money("-1"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private static Money money(String amount) {
        return new Money(new BigDecimal(amount));
    }
}
