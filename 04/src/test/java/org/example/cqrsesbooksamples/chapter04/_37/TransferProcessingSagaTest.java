package org.example.cqrsesbooksamples.chapter04._37;

import java.math.BigDecimal;

import org.axonframework.test.saga.SagaTestFixture;
import org.example.cqrsesbooksamples.chapter04._34.AccountId;
import org.example.cqrsesbooksamples.chapter04._34.Money;
import org.example.cqrsesbooksamples.chapter04._37.commands.CompleteTransferCommand;
import org.example.cqrsesbooksamples.chapter04._37.commands.CreditAccountCommand;
import org.example.cqrsesbooksamples.chapter04._37.commands.DebitAccountCommand;
import org.example.cqrsesbooksamples.chapter04._37.commands.FailTransferCommand;
import org.example.cqrsesbooksamples.chapter04._37.commands.RefundAccountCommand;
import org.example.cqrsesbooksamples.chapter04._37.events.AccountCreditFailedEvent;
import org.example.cqrsesbooksamples.chapter04._37.events.AccountCreditedEvent;
import org.example.cqrsesbooksamples.chapter04._37.events.AccountDebitFailedEvent;
import org.example.cqrsesbooksamples.chapter04._37.events.AccountDebitedEvent;
import org.example.cqrsesbooksamples.chapter04._37.events.AccountRefundedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransferProcessingSagaTest {
    private static final TransferId TRANSFER_ID = new TransferId("transfer-1");
    private static final AccountId SOURCE = new AccountId("A");
    private static final AccountId TARGET = new AccountId("B");
    private static final Money AMOUNT = new Money(new BigDecimal("300"));

    private SagaTestFixture<TransferProcessingSaga> fixture;

    @BeforeEach
    void setUp() {
        fixture = new SagaTestFixture<>(TransferProcessingSaga.class);
    }

    @Test
    void 送金登録で送金元への引き落としコマンドが送られる() {
        fixture.givenNoPriorActivity()
            .whenAggregate(TRANSFER_ID.value()).publishes(
                new TransferRegisteredEvent(TRANSFER_ID, SOURCE, TARGET, AMOUNT))
            .expectActiveSagas(1)
            .expectDispatchedCommands(new DebitAccountCommand(SOURCE, AMOUNT, TRANSFER_ID));
    }

    @Test
    void 引き落とし成功で送金先への入金コマンドが送られる() {
        fixture.givenAggregate(TRANSFER_ID.value()).published(
                new TransferRegisteredEvent(TRANSFER_ID, SOURCE, TARGET, AMOUNT))
            .whenPublishingA(new AccountDebitedEvent(SOURCE, AMOUNT, TRANSFER_ID))
            .expectDispatchedCommands(new CreditAccountCommand(TARGET, AMOUNT, TRANSFER_ID));
    }

    @Test
    void 入金成功で送金が完了しサガが終了する() {
        fixture.givenAggregate(TRANSFER_ID.value()).published(
                new TransferRegisteredEvent(TRANSFER_ID, SOURCE, TARGET, AMOUNT),
                new AccountDebitedEvent(SOURCE, AMOUNT, TRANSFER_ID))
            .whenPublishingA(new AccountCreditedEvent(TARGET, AMOUNT, TRANSFER_ID))
            .expectDispatchedCommands(new CompleteTransferCommand(TRANSFER_ID))
            .expectActiveSagas(0);
    }

    @Test
    void 引き落とし失敗では巻き戻しなしで送金失敗を記録する() {
        fixture.givenAggregate(TRANSFER_ID.value()).published(
                new TransferRegisteredEvent(TRANSFER_ID, SOURCE, TARGET, AMOUNT))
            .whenPublishingA(new AccountDebitFailedEvent(SOURCE, TRANSFER_ID, "balance too low"))
            .expectDispatchedCommands(new FailTransferCommand(TRANSFER_ID, "balance too low"))
            .expectActiveSagas(0);
    }

    @Test
    void 入金失敗では返金コマンドを送りサガは返金の記録を待つ() {
        fixture.givenAggregate(TRANSFER_ID.value()).published(
                new TransferRegisteredEvent(TRANSFER_ID, SOURCE, TARGET, AMOUNT),
                new AccountDebitedEvent(SOURCE, AMOUNT, TRANSFER_ID))
            .whenPublishingA(new AccountCreditFailedEvent(TARGET, TRANSFER_ID, "account closed"))
            .expectDispatchedCommands(new RefundAccountCommand(SOURCE, AMOUNT, TRANSFER_ID))
            .expectActiveSagas(1);
    }

    @Test
    void 返金が記録されてから送金失敗を確定しサガが終了する() {
        fixture.givenAggregate(TRANSFER_ID.value()).published(
                new TransferRegisteredEvent(TRANSFER_ID, SOURCE, TARGET, AMOUNT),
                new AccountDebitedEvent(SOURCE, AMOUNT, TRANSFER_ID),
                new AccountCreditFailedEvent(TARGET, TRANSFER_ID, "account closed"))
            .whenPublishingA(new AccountRefundedEvent(SOURCE, AMOUNT, TRANSFER_ID))
            .expectDispatchedCommands(new FailTransferCommand(TRANSFER_ID, "入金失敗: account closed"))
            .expectActiveSagas(0);
    }
}
