// 紙面照合: 7.13
package org.example.cqrsesbooksamples.chapter07._15;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessAcceptedEvent;
import org.example.cqrsesbooksamples.chapter07.support.PaymentMethod;
import org.example.cqrsesbooksamples.chapter07.support.RequestBankTransferCommand;
import org.springframework.stereotype.Component;


@Component
public class BankTransferHandler {
    private final CommandGateway commandGateway;

    public BankTransferHandler(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @EventHandler
    public void on(PaymentProcessAcceptedEvent event) {
        if (event.paymentMethod() == PaymentMethod.BANK_TRANSFER) {
            commandGateway.send(new RequestBankTransferCommand(
                event.paymentProcessId(),
                event.accountId(),
                event.amount()));
        }
    }
}
