// 検証補助: 7.14
package org.example.cqrsesbooksamples.chapter07._16;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.axonframework.eventhandling.AnnotationEventHandlerAdapter;
import org.axonframework.eventhandling.GenericEventMessage;
import org.junit.jupiter.api.Test;

/**
 * 注文システムに依存する決済サービス（リスト7.14、悪い例）の検証。
 * 注文システムのイベント型と注文種別の分岐を決済側が抱え込む形が
 * 実際に動く（=依存が成立してしまう）ことを確かめる。
 */
class PaymentServiceTest {

    @Test
    void 注文システムのイベントと注文種別分岐を決済側で処理してしまう() {
        var service = new PaymentService();
        var handlerAdapter = new AnnotationEventHandlerAdapter(service);

        assertThatCode(() -> {
            // 注文作成イベント（通常・サブスクリプションの両方の分岐が決済側で動く）
            handlerAdapter.handle(GenericEventMessage.asEventMessage(
                    new OrderCreatedEvent("order-1", OrderType.NORMAL)));
            handlerAdapter.handle(GenericEventMessage.asEventMessage(
                    new OrderCreatedEvent("order-2", OrderType.SUBSCRIPTION)));
            // 注文キャンセルという注文側の業務契機にも反応してしまう
            handlerAdapter.handle(GenericEventMessage.asEventMessage(
                    new OrderCancelledEvent("order-1")));
        }).doesNotThrowAnyException();
    }
}
