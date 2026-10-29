// 紙面照合: 6.5
package org.example.cqrsesbooksamples.chapter06._01;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.DisallowReplay;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;


@Component
public record PaymentProcessCreditPaymentHandler(CommandGateway commandGateway) {
    @DisallowReplay
    @EventHandler
    public void on(PaymentProcessAcceptedEvent event) {
        if (event.paymentMethod() == PaymentMethod.CREDIT) {
            commandGateway.send(new ExecuteCreditPaymentCommand(event.paymentProcessId(), event.accountId(), event.amount()));
        }
    }
}
