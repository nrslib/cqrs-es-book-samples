package org.example.cqrsesbooksamples.chapter08._21;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class OrderAggregate {
    @AggregateIdentifier
    private String orderId;

    public OrderAggregate() {
    }

    @CommandHandler
    public OrderAggregate(CreateOrderCommand command) {
        // 金額の業務検証は読み取り側のハンドラが担う（DLQ退避経路の題材のため）
        AggregateLifecycle.apply(new OrderCreatedEvent(command.orderId(), command.amount()));
    }

    @CommandHandler
    public void handle(ProcessPaymentCommand command) {
        AggregateLifecycle.apply(new PaymentProcessedEvent(command.orderId()));
    }

    @CommandHandler
    public void handle(CompleteOrderCommand command) {
        AggregateLifecycle.apply(new OrderCompletedEvent(command.orderId()));
    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent event) {
        this.orderId = event.getOrderId();
    }

    @EventSourcingHandler
    public void on(PaymentProcessedEvent event) {
        // 状態フィールドの更新は不要
    }

    @EventSourcingHandler
    public void on(OrderCompletedEvent event) {
        // 状態フィールドの更新は不要
    }
}
