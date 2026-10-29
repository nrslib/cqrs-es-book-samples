// 紙面照合: 6.15
package org.example.cqrsesbooksamples.chapter06._01;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.modelling.command.Repository;
import org.springframework.stereotype.Service;


@Service
public record NotificationService(
    EventGateway eventGateway,
    Repository<PaymentProcessAggregateAdapter> paymentAggregateRepository
) {
    @CommandHandler
    public void handle(SendPaymentCompletionNotificationCommand command) {
        paymentAggregateRepository.load(command.paymentProcessId().asString())
                .execute(adapter -> {
                    var payment = adapter.getAggregate();
                    // Call http client to send notification.
                    eventGateway.publish(new PaymentCompletionNotifiedEvent(
                        command.paymentProcessId(),
                        payment.accountId()
                    ));
                });
    }
}
