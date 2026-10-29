package org.example.cqrsesbooksamples.chapter08._15;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
public class OrderSummaryProjection {
    private final OrderSummaryRepository repository;

    public OrderSummaryProjection(OrderSummaryRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(OrderCreatedEvent event) {
        var summary = new OrderSummary(event.orderId());
        summary.setAmount(event.amount());
        repository.save(summary);
    }

    @EventHandler
    public void on(PaymentCompletedEvent event) {
        var summary = repository.findById(event.orderId()).orElseThrow();
        summary.setPaymentCompleted(true);
        repository.save(summary);
    }

    @EventHandler
    public void on(OrderShippedEvent event) {
        var summary = repository.findById(event.orderId()).orElseThrow();
        summary.setStatus(OrderStatus.SHIPPED);
        repository.save(summary);
    }
}
