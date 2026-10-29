package org.example.cqrsesbooksamples.chapter10._01;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.axonframework.eventhandling.AnnotationEventHandlerAdapter;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventhandling.async.SequentialPerAggregatePolicy;
import org.junit.jupiter.api.Test;

/**
 * スケールアウト可能なイベントハンドラ（旧 10.1）と、
 * 集約をまたぐイベントを受けるハンドラ（旧 10.2）の検証。
 * どちらも状態を持たずにイベントを処理できることを確かめる。
 */
class EventHandlerScalingTest {

    @Test
    void 状態を持たないハンドラとしてイベントを処理できる() {
        var handlerAdapter = new AnnotationEventHandlerAdapter(new HighLoadEventHandler());

        assertThatCode(() -> handlerAdapter.handle(GenericEventMessage.asEventMessage(
                new OrderCreatedEvent("order-1"))))
                .doesNotThrowAnyException();
    }

    @Test
    void 異なる集約由来のイベントを同じハンドラで受け取れる() {
        var handlerAdapter = new AnnotationEventHandlerAdapter(new CrossAggregateEventHandler());

        assertThatCode(() -> {
            handlerAdapter.handle(GenericEventMessage.asEventMessage(
                    new OrderCreatedEvent("order-1")));
            handlerAdapter.handle(GenericEventMessage.asEventMessage(
                    new PaymentProcessedEvent("order-1")));
        }).doesNotThrowAnyException();
    }

    @Test
    void 異なる集約のイベントは別の順序制御単位に振り分けられる() {
        var policy = SequentialPerAggregatePolicy.instance();
        var orderEvent = new GenericDomainEventMessage<>(
                "Order", "order-1", 0, new OrderCreatedEvent("order-1"));
        var paymentEvent = new GenericDomainEventMessage<>(
                "Payment", "payment-1", 0, new PaymentProcessedEvent("order-1"));

        assertThat(policy.getSequenceIdentifierFor(orderEvent))
                .isNotEqualTo(policy.getSequenceIdentifierFor(paymentEvent));
    }
}
