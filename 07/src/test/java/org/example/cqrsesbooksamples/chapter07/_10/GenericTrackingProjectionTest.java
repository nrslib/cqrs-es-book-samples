// 検証補助: 7.8
package org.example.cqrsesbooksamples.chapter07._10;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.axonframework.eventhandling.AnnotationEventHandlerAdapter;
import org.axonframework.eventhandling.GenericEventMessage;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessAcceptedEvent;
import org.example.cqrsesbooksamples.chapter07._09.PaymentProcessTracker;
import org.example.cqrsesbooksamples.chapter07._09.PaymentProcessTrackerRepository;
import org.example.cqrsesbooksamples.chapter07.support.AccountId;
import org.example.cqrsesbooksamples.chapter07.support.PaymentMethod;
import org.example.cqrsesbooksamples.chapter07.support.PaymentProcessId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * ジェネリックなハンドラにまとめたプロジェクション（リスト7.8）の検証。
 * どの型のイベントでもメタデータの processId を手掛かりに追跡され、
 * processId がないイベントは無視されることを確かめる。
 */
class GenericTrackingProjectionTest {

    private final PaymentProcessTrackerRepository repository =
            mock(PaymentProcessTrackerRepository.class);
    private final PaymentProcessTrackingProjection projection =
            new PaymentProcessTrackingProjection(repository);

    private PaymentProcessAcceptedEvent acceptedEvent() {
        return new PaymentProcessAcceptedEvent(
                new PaymentProcessId(), new AccountId(UUID.randomUUID()),
                new BigDecimal("5000"), PaymentMethod.CREDIT);
    }

    @Test
    void メタデータのprocessIdを手掛かりにイベントを追跡する() throws Exception {
        when(repository.findById("process-1")).thenReturn(Optional.empty());
        var handlerAdapter = new AnnotationEventHandlerAdapter(projection);

        handlerAdapter.handle(GenericEventMessage.asEventMessage(acceptedEvent())
                .withMetaData(Map.of("processId", "process-1")));

        var captor = ArgumentCaptor.forClass(PaymentProcessTracker.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getProcessId()).isEqualTo("process-1");
        assertThat(captor.getValue().getCurrentStatus()).isEqualTo("STARTED");
        assertThat(captor.getValue().getEvents()).hasSize(1);
    }

    @Test
    void processIdがないイベントは無視される() throws Exception {
        var handlerAdapter = new AnnotationEventHandlerAdapter(projection);

        handlerAdapter.handle(GenericEventMessage.asEventMessage(acceptedEvent()));

        verify(repository, never()).save(any());
    }
}
