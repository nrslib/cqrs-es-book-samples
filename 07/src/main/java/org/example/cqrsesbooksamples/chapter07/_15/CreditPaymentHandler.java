// 紙面照合: 7.13
package org.example.cqrsesbooksamples.chapter07._15;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.example.cqrsesbooksamples.chapter07._01.CreditAcceptedEvent;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessAcceptedEvent;
import org.example.cqrsesbooksamples.chapter07.support.CompletePaymentProcessCommand;
import org.example.cqrsesbooksamples.chapter07.support.ExecuteCreditPaymentCommand;
import org.example.cqrsesbooksamples.chapter07.support.PaymentMethod;
import org.springframework.stereotype.Component;


@Component
public class CreditPaymentHandler {
    private final CommandGateway commandGateway;

    public CreditPaymentHandler(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @EventHandler
    public void on(PaymentProcessAcceptedEvent event) {
        if (event.paymentMethod() == PaymentMethod.CREDIT) {
            commandGateway.send(new ExecuteCreditPaymentCommand(
                event.paymentProcessId(),
                event.accountId(),
                event.amount()));
        }
    }

    @EventHandler
    public void on(CreditAcceptedEvent event) {
        commandGateway.send(new CompletePaymentProcessCommand(
            event.paymentProcessId()));
    }
}
