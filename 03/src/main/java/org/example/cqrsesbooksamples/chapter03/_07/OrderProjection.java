package org.example.cqrsesbooksamples.chapter03._07;

import org.axonframework.eventhandling.EventHandler;

public class OrderProjection {
    private final OrderSummaryRepository orderSummaryRepository;

    public OrderProjection(OrderSummaryRepository orderSummaryRepository) {
        this.orderSummaryRepository = orderSummaryRepository;
    }

    // 削除済み（旧 3.7）
    @EventHandler
    public void on(OrderPlacedEvent event) {
        // Axon Frameworkが自動的にトランザクション内で実行する
        orderSummaryRepository.save(new OrderSummary(event.getOrderId()));
    }
}
