package org.example.cqrsesbooksamples.chapter08._21;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

/**
 * 注文状況のリードモデル。
 * 金額の業務検証もこのグループで行い、不正な注文はDLQへ退避させる。
 */
@Component
@ProcessingGroup("order-projection")
public class OrderProjection {
    private final OrderStatusRepository repository;

    public OrderProjection(OrderStatusRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(OrderCreatedEvent event) {
        if (event.getAmount() < 0) {
            throw new IllegalArgumentException("INVALID_AMOUNT");
        }
        repository.save(new OrderStatusEntry(event.getOrderId(), OrderStatus.CREATED));
    }

    @EventHandler
    public void on(OrderCompletedEvent event) {
        repository.findById(event.getOrderId()).ifPresent(entry -> {
            entry.setStatus(OrderStatus.COMPLETED);
            repository.save(entry);
        });
    }

    @QueryHandler
    public OrderStatus handle(GetOrderStatusQuery query) {
        return repository.findById(query.orderId())
            .map(OrderStatusEntry::getStatus)
            .orElse(null);
    }
}
