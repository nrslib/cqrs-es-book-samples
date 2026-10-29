package org.example.cqrsesbooksamples.chapter06._10;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.example.cqrsesbooksamples.chapter06._01.AccountId;
import org.example.cqrsesbooksamples.chapter06._01.ExecuteCreditPaymentCommand;
import org.example.cqrsesbooksamples.chapter06._01.CreditMakePaymentFailedEvent;
import org.example.cqrsesbooksamples.chapter06._01.PaymentProcessId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreditRetrySchedulingHandlerTest {
    private EventScheduler eventScheduler;
    private EventGateway eventGateway;
    private CreditRetrySchedulingHandler step;

    @BeforeEach
    void setUp() {
        eventScheduler = mock(EventScheduler.class);
        eventGateway = mock(EventGateway.class);
        step = new CreditRetrySchedulingHandler(eventScheduler, eventGateway);
    }

    @Test
    void 失敗イベントを受けると指数バックオフで再試行が予約される() {
        var command = new ExecuteCreditPaymentCommand(new PaymentProcessId(), new AccountId(UUID.randomUUID()),
            new BigDecimal("1000"), 2);

        step.on(new CreditMakePaymentFailedEvent(command, "接続エラー"));

        // 2回目の失敗 → 待ち時間は 5 << 2 = 20秒、countは3に増える
        verify(eventScheduler).schedule(
            Duration.ofSeconds(20),
            new ExternalRetryRequestedEvent(new ExecuteCreditPaymentCommand(
                command.paymentProcessId(), command.accountId(), command.amount(), 3)));
    }

    @Test
    void 上限に達すると再試行を打ち切って運用フローへ引き渡す() {
        var command = new ExecuteCreditPaymentCommand(new PaymentProcessId(), new AccountId(UUID.randomUUID()),
            new BigDecimal("1000"), 5);

        step.on(new CreditMakePaymentFailedEvent(command, "接続エラー"));

        verify(eventGateway).publish(new CreditMakePaymentCriticalErrorOccurredEvent());
        verify(eventScheduler, never()).schedule(any(Duration.class), any());
    }
}
