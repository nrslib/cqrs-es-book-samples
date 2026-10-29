// 検証補助: 3.47
package org.example.cqrsesbooksamples.chapter03._64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.axonframework.eventhandling.AnnotationEventHandlerAdapter;
import org.axonframework.eventhandling.GenericEventMessage;
import org.junit.jupiter.api.Test;

/**
 * べき等でないプロジェクション（リスト3.46）が問題を起こすことの検証。
 * 同じイベントが重複して届くと、件数と金額が二重に集計されることを確かめる。
 */
class NonIdempotentProjectionTest {

    @Test
    void 同じイベントの再配信で件数と金額が二重に集計されてしまう() throws Exception {
        var repository = mock(OrderSummaryRepository.class);
        var summary = new OrderSummary("2026-08");
        when(repository.findByMonth("2026-08")).thenReturn(summary);

        var projection = new NonIdempotentOrderSummaryProjection(repository);
        var handlerAdapter = new AnnotationEventHandlerAdapter(projection);
        var event = GenericEventMessage.asEventMessage(new OrderPlacedEvent(
                "event-1", "order-1", "2026-08", new BigDecimal("1000"), "john@example.com"));

        // 同じイベントが2回届く（リトライや再配信で起こりうる）
        handlerAdapter.handle(event);
        handlerAdapter.handle(event);

        // 1件の注文が2件として集計され、金額も2倍になってしまう
        assertThat(summary.getOrderCount()).isEqualTo(2);
        assertThat(summary.getTotalAmount()).isEqualByComparingTo(new BigDecimal("2000"));
    }
}
