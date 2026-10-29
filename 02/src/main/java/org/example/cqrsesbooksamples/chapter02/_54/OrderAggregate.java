package org.example.cqrsesbooksamples.chapter02._54;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

// 削除済み（旧 2.54）
@Aggregate
public class OrderAggregate {
    @AggregateIdentifier
    private String orderId;

    public OrderAggregate() {
    }

    @CommandHandler
    public OrderAggregate(PlaceOrderCommand command) {
        apply(new OrderPlacedEvent(command.orderId(), command.amount()));
    }

    @EventSourcingHandler
    public void on(OrderPlacedEvent event) {
        this.orderId = event.orderId();
    }
}
