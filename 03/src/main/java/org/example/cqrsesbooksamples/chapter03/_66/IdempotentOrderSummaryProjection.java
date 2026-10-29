// 紙面照合: 3.49
package org.example.cqrsesbooksamples.chapter03._66;

import org.axonframework.eventhandling.EventHandler;

public class IdempotentOrderSummaryProjection {
    private final OrderSummaryRepository orderSummaryRepository;
    private final ProcessedEventRepository processedEventRepository;

    public IdempotentOrderSummaryProjection(OrderSummaryRepository orderSummaryRepository,
                                            ProcessedEventRepository processedEventRepository) {
        this.orderSummaryRepository = orderSummaryRepository;
        this.processedEventRepository = processedEventRepository;
    }


    @EventHandler
    public void on(OrderPlacedEvent event) {
        // すでに処理済みかチェック
        if (processedEventRepository.existsByEventId(event.eventId())) {
            return;  // すでに処理済みなのでスキップ
        }

        // 処理を実行
        var summary = orderSummaryRepository.findByMonth(event.orderMonth());
        summary.incrementOrderCount();
        summary.addAmount(event.amount());
        orderSummaryRepository.save(summary);

        // 処理済みとして記録
        processedEventRepository.save(new ProcessedEvent(event.eventId()));
    }
}
