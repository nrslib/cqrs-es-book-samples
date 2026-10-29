// 検証補助: 4.25
package org.example.cqrsesbooksamples.chapter04._29;

import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;


@Aggregate
public class OrderAggregateAdapter {
    @AggregateIdentifier
    private String orderId() {
        return order == null ? null : order.id();
    }
    private Order order;

    public OrderAggregateAdapter() {
    }

    @CommandHandler
    public OrderAggregateAdapter(CreateOrderCommand command) {
        var orderId = UUID.randomUUID().toString();
        var event = Order.create(orderId, command.orderLines());
        AggregateLifecycle.apply(event);
    }

    @CommandHandler
    public void handle(CompleteOrderCommand command) {
        AggregateLifecycle.apply(order.complete());
    }

    @CommandHandler
    public void handle(CancelOrderCommand command) {
        AggregateLifecycle.apply(order.cancel());
    }

    @EventSourcingHandler
    public void on(OrderEvent event) {
        if (order != null) {
            order = order.applyEvent(event);
        } else {
            if (event instanceof OrderCreatedEvent e) {
                order = Order.applyEvent(e);
            } else {
                throw new IllegalStateException("Expected OrderCreatedEvent");
            }
        }
    }
}
