// 紙面照合: 6.18
package org.example.cqrsesbooksamples.chapter06._01;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.DisallowReplay;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;


@Component
public record BankTransferCompleteHandler(CommandGateway commandGateway) {
    @DisallowReplay
    @EventHandler
    public void on(BankTransferDetectedEvent event) {
        commandGateway.send(new CompletePaymentProcessCommand(event.paymentProcessId()));
    }
}
