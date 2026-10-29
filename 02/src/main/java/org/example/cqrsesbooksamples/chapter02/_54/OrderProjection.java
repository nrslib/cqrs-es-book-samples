package org.example.cqrsesbooksamples.chapter02._54;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

// 削除済み（旧 2.59）
@Component
public class OrderProjection {
    private final OrderReadModelRepository repository;

    public OrderProjection(OrderReadModelRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(OrderPlacedEvent event) {
        // リードモデルを更新
        repository.save(new OrderReadModel(event.orderId(), event.amount()));
    }
}
