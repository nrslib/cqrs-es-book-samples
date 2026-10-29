package org.example.cqrsesbooksamples.chapter06._01;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.DisallowReplay;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

// 削除済み（旧 6.17）
@Component
public record PaymentProcessNotificationHandler(CommandGateway commandGateway) {
    @DisallowReplay
    @EventHandler
    public void on(PaymentProcessCompletedEvent event) {
        commandGateway.send(new SendPaymentCompletionNotificationCommand(event.paymentProcessId()));
    }
}
