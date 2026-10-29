// 紙面照合: 3.5
package org.example.cqrsesbooksamples.chapter03._09;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.example.cqrsesbooksamples.chapter03._08.support.OrderPlacedEvent;
import org.example.cqrsesbooksamples.chapter03._08.support.OrderRepository;
import org.example.cqrsesbooksamples.chapter03._08.support.OrderSummary;


// 複数のクラスでも同じProcessingGroupを指定すれば同じトランザクションになる
@ProcessingGroup("order-projection")
public class OrderSummaryProjection {
    private final OrderRepository orderRepository;

    public OrderSummaryProjection(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @EventHandler
    public void on(OrderPlacedEvent event) {
        orderRepository.save(new OrderSummary(event.getOrderId()));
    }
}
