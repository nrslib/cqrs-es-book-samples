package org.example.cqrsesbooksamples.chapter07._14;

import java.math.BigDecimal;
import java.util.UUID;

import org.axonframework.test.saga.SagaTestFixture;
import org.example.cqrsesbooksamples.chapter07._01.CreditAcceptedEvent;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessAcceptedEvent;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessCompletedEvent;
import org.example.cqrsesbooksamples.chapter07.support.AccountId;
import org.example.cqrsesbooksamples.chapter07.support.CancelPaymentProcessCommand;
import org.example.cqrsesbooksamples.chapter07.support.CompletePaymentProcessCommand;
import org.example.cqrsesbooksamples.chapter07.support.ExecuteCreditPaymentCommand;
import org.example.cqrsesbooksamples.chapter07.support.CreditRejectedEvent;
import org.example.cqrsesbooksamples.chapter07.support.NotificationPaymentExecutionSentEvent;
import org.example.cqrsesbooksamples.chapter07.support.SendPaymentCompletionNotificationCommand;
import org.example.cqrsesbooksamples.chapter07.support.PaymentMethod;
import org.example.cqrsesbooksamples.chapter07.support.PaymentProcessId;
import org.example.cqrsesbooksamples.chapter07.support.RequestBankTransferCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentProcessSagaTest {
    private static final PaymentProcessId PROCESS_ID = new PaymentProcessId();
    private static final AccountId ACCOUNT_ID = new AccountId(UUID.randomUUID());
    private static final BigDecimal AMOUNT = new BigDecimal("5000");

    private SagaTestFixture<PaymentProcessSaga> fixture;

    @BeforeEach
    void setUp() {
        fixture = new SagaTestFixture<>(PaymentProcessSaga.class);
    }

    @Test
    void クレジット決済の開始でExecuteCreditPaymentCommandが送られる() {
        fixture.givenNoPriorActivity()
            .whenAggregate(PROCESS_ID.value()).publishes(
                new PaymentProcessAcceptedEvent(PROCESS_ID, ACCOUNT_ID, AMOUNT, PaymentMethod.CREDIT))
            .expectActiveSagas(1)
            .expectDispatchedCommands(new ExecuteCreditPaymentCommand(PROCESS_ID, ACCOUNT_ID, AMOUNT));
    }

    @Test
    void 銀行振込の開始でRequestBankTransferCommandが送られる() {
        fixture.givenNoPriorActivity()
            .whenAggregate(PROCESS_ID.value()).publishes(
                new PaymentProcessAcceptedEvent(PROCESS_ID, ACCOUNT_ID, AMOUNT, PaymentMethod.BANK_TRANSFER))
            .expectDispatchedCommands(new RequestBankTransferCommand(PROCESS_ID, ACCOUNT_ID, AMOUNT));
    }

    @Test
    void 決済承認で完了コマンドが送られる() {
        fixture.givenAggregate(PROCESS_ID.value()).published(
                new PaymentProcessAcceptedEvent(PROCESS_ID, ACCOUNT_ID, AMOUNT, PaymentMethod.CREDIT))
            .whenPublishingA(new CreditAcceptedEvent(PROCESS_ID))
            .expectDispatchedCommands(new CompletePaymentProcessCommand(PROCESS_ID));
    }

    @Test
    void 決済完了後に通知コマンドが送られる() {
        fixture.givenAggregate(PROCESS_ID.value()).published(
                new PaymentProcessAcceptedEvent(PROCESS_ID, ACCOUNT_ID, AMOUNT, PaymentMethod.CREDIT),
                new CreditAcceptedEvent(PROCESS_ID))
            .whenPublishingA(new PaymentProcessCompletedEvent(PROCESS_ID))
            .expectDispatchedCommands(new SendPaymentCompletionNotificationCommand(PROCESS_ID));
    }

    @Test
    void 通知完了後にサガが終了する() {
        fixture.givenAggregate(PROCESS_ID.value()).published(
                new PaymentProcessAcceptedEvent(PROCESS_ID, ACCOUNT_ID, AMOUNT, PaymentMethod.CREDIT),
                new CreditAcceptedEvent(PROCESS_ID),
                new PaymentProcessCompletedEvent(PROCESS_ID))
            .whenPublishingA(new NotificationPaymentExecutionSentEvent(PROCESS_ID))
            .expectActiveSagas(0);
    }

    @Test
    void 決済拒否でキャンセルされサガが終了する() {
        fixture.givenAggregate(PROCESS_ID.value()).published(
                new PaymentProcessAcceptedEvent(PROCESS_ID, ACCOUNT_ID, AMOUNT, PaymentMethod.CREDIT))
            .whenPublishingA(new CreditRejectedEvent(PROCESS_ID))
            .expectDispatchedCommands(new CancelPaymentProcessCommand(PROCESS_ID, "クレジットカード決済が拒否されました"))
            .expectActiveSagas(0);
    }
}
