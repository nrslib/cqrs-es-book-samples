// 紙面照合: 7.13
package org.example.cqrsesbooksamples.chapter07._15;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessCompletedEvent;
import org.example.cqrsesbooksamples.chapter07.support.SendPaymentCompletionNotificationCommand;
import org.springframework.stereotype.Component;


@Component
public class PaymentNotificationHandler {
    private final CommandGateway commandGateway;

    public PaymentNotificationHandler(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @EventHandler
    public void on(PaymentProcessCompletedEvent event) {
        commandGateway.send(new SendPaymentCompletionNotificationCommand(
            event.paymentProcessId()));
    }
}
