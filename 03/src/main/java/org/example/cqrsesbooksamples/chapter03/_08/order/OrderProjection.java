package org.example.cqrsesbooksamples.chapter03._08.order;

import org.axonframework.eventhandling.EventHandler;
import org.example.cqrsesbooksamples.chapter03._08.support.OrderPlacedEvent;
import org.example.cqrsesbooksamples.chapter03._08.support.OrderRepository;
import org.example.cqrsesbooksamples.chapter03._08.support.OrderSummary;

// 削除済み（旧 3.8）
// 誤った例：異なるパッケージにあるため、異なるトランザクションで実行される
public class OrderProjection {
    private final OrderRepository orderRepository;

    public OrderProjection(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @EventHandler
    public void on(OrderPlacedEvent event) {
        orderRepository.save(new OrderSummary(event.getOrderId()));
    }
}
