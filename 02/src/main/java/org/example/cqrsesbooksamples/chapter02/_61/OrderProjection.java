// 紙面照合: 2.48
package org.example.cqrsesbooksamples.chapter02._61;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;


@Component
@ProcessingGroup("order-projection")
public class OrderProjection {
    private final OrderReadModelRepository repository;

    public OrderProjection(OrderReadModelRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(OrderPlacedEvent event) {
        repository.save(new OrderReadModel(event.orderId(), event.amount()));
    }
}
