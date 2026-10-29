package org.example.cqrsesbooksamples.chapter04._36;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.example.cqrsesbooksamples.chapter04._34.Account;
import org.example.cqrsesbooksamples.chapter04._34.AccountId;
import org.example.cqrsesbooksamples.chapter04._34.AccountNotFoundException;
import org.example.cqrsesbooksamples.chapter04._34.AccountRepository;
import org.example.cqrsesbooksamples.chapter04._34.Money;
import org.junit.jupiter.api.Test;

/**
 * リポジトリを使わないドメインサービスとアプリケーションサービスの分担
 * （旧 4.36）の検証。取得と保存はアプリケーション層が担い、
 * ドメインサービスは口座間の資金移動だけを行うことを確かめる。
 */
class TransferApplicationServiceTest {

    private final AccountRepository repository = mock(AccountRepository.class);
    private final TransferApplicationService service =
            new TransferApplicationService(repository, new MoneyTransferService());

    @Test
    void 口座を取得して資金を移動し保存する() {
        var source = new Account(new AccountId("account-1"), new Money(new BigDecimal("1000")));
        var target = new Account(new AccountId("account-2"), new Money(new BigDecimal("500")));
        when(repository.findById(new AccountId("account-1"))).thenReturn(Optional.of(source));
        when(repository.findById(new AccountId("account-2"))).thenReturn(Optional.of(target));

        service.transfer(new AccountId("account-1"), new AccountId("account-2"),
                new Money(new BigDecimal("300")));

        assertThat(source.getBalance()).isEqualTo(new Money(new BigDecimal("700")));
        assertThat(target.getBalance()).isEqualTo(new Money(new BigDecimal("800")));
        verify(repository).save(source);
        verify(repository).save(target);
    }

    @Test
    void 口座が見つからなければ例外になる() {
        when(repository.findById(new AccountId("missing"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.transfer(
                new AccountId("missing"), new AccountId("account-2"),
                new Money(new BigDecimal("300"))))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
