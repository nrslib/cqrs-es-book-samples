// 紙面照合: 3.47
package org.example.cqrsesbooksamples.chapter03._64;

import org.axonframework.eventhandling.EventHandler;

// べき等でない実装例（問題あり）
public class NonIdempotentOrderSummaryProjection {
    private final OrderSummaryRepository orderSummaryRepository;

    public NonIdempotentOrderSummaryProjection(OrderSummaryRepository orderSummaryRepository) {
        this.orderSummaryRepository = orderSummaryRepository;
    }


    @EventHandler
    public void on(OrderPlacedEvent event) {
        var summary = orderSummaryRepository.findByMonth(event.orderMonth());
        summary.incrementOrderCount();  // 重複処理で2回カウントされる
        summary.addAmount(event.amount());  // 金額も2倍になる
        orderSummaryRepository.save(summary);
    }
}
