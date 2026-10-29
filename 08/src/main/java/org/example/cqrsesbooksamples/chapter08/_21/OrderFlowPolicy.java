package org.example.cqrsesbooksamples.chapter08._21;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

/**
 * 注文フローを進めるポリシー。
 * 注文作成で支払いを要求し、支払い完了で注文を確定する。
 */
@Component
@ProcessingGroup("order-flow")
public class OrderFlowPolicy {
    private final CommandGateway commandGateway;

    public OrderFlowPolicy(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @EventHandler
    public void on(OrderCreatedEvent event) {
        commandGateway.send(new ProcessPaymentCommand(event.getOrderId(), event.getAmount()));
    }

    @EventHandler
    public void on(PaymentProcessedEvent event) {
        commandGateway.send(new CompleteOrderCommand(event.getOrderId()));
    }
}
