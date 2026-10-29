package org.example.cqrsesbooksamples.chapter07._18;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.axonframework.eventhandling.AnnotationEventHandlerAdapter;
import org.axonframework.eventhandling.GenericEventMessage;
import org.example.cqrsesbooksamples.chapter07._16.OrderCreatedEvent;
import org.example.cqrsesbooksamples.chapter07._16.OrderType;
import org.junit.jupiter.api.Test;

/**
 * 他システムのイベント型に依存する避けるべき実装（旧 7.18）の検証。
 * 別システム（注文）が定義したイベント型をこちら側のハンドラで
 * 受け取れてしまう（=型への依存が成立する）ことを確かめる。
 */
class PaymentEventHandlerTest {

    @Test
    void 注文システムのイベント型をハンドリングできてしまう() {
        var handler = new PaymentEventHandler();
        var handlerAdapter = new AnnotationEventHandlerAdapter(handler);

        assertThatCode(() -> handlerAdapter.handle(GenericEventMessage.asEventMessage(
                new OrderCreatedEvent("order-1", OrderType.NORMAL))))
                .doesNotThrowAnyException();
    }
}
