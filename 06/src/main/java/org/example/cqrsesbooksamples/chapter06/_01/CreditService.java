// 紙面照合: 6.7
package org.example.cqrsesbooksamples.chapter06._01;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Service;


@Service
public record CreditService(CreditApi creditApi, QueryGateway queryGateway, EventGateway eventGateway) {
    @CommandHandler
    public void handle(ExecuteCreditPaymentCommand command) {
        var response = creditApi.makePayment(new CreditMakePaymentRequest(
            command.paymentProcessId(), command.accountId(), command.amount()));
        if (response.statusCode() != 200) {
            eventGateway.publish(new CreditMakePaymentFailedEvent(command, null));
            return;
        }
        if (response.accepted()) {
            eventGateway.publish(new CreditAcceptedEvent(command.paymentProcessId()));
        } else {
            eventGateway.publish(new CreditRejectedEvent(command.paymentProcessId()));
        }
    }
}
