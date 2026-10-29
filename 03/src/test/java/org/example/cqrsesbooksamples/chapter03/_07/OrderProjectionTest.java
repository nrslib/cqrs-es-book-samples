package org.example.cqrsesbooksamples.chapter03._07;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.axonframework.eventhandling.AnnotationEventHandlerAdapter;
import org.axonframework.eventhandling.GenericEventMessage;
import org.junit.jupiter.api.Test;

/**
 * トランザクション内で実行されるプロジェクション（旧 3.7）の検証。
 * ＠EventHandlerの解決を経由してイベントが届き、リードモデルが保存されることを確かめる。
 */
class OrderProjectionTest {

    @Test
    void イベントを受け取って注文サマリーを保存する() throws Exception {
        var repository = mock(OrderSummaryRepository.class);
        var projection = new OrderProjection(repository);
        var handlerAdapter = new AnnotationEventHandlerAdapter(projection);

        handlerAdapter.handle(GenericEventMessage.asEventMessage(
                new OrderPlacedEvent("order-1", "product-1")));

        verify(repository).save(new OrderSummary("order-1"));
    }
}
