// 紙面照合: 6.3, 6.13
package org.example.cqrsesbooksamples.chapter06._01;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;


@Aggregate
public class PaymentProcessAggregateAdapter {
    private PaymentProcess paymentProcess;

    @AggregateIdentifier
    public PaymentProcessId paymentProcessId() {
        if (paymentProcess == null) {
            return null;
        }
        return paymentProcess.paymentProcessId();
    }

    public PaymentProcess getAggregate() {
        return paymentProcess;
    }

    protected PaymentProcessAggregateAdapter() {
    }


    @CommandHandler
    public PaymentProcessAggregateAdapter(StartPaymentCommand command) {
        var accountId = new AccountId(command.accountId());
        var paymentProcessId = command.processId() == null
            ? new PaymentProcessId()
            : new PaymentProcessId(command.processId().toString());
        var event = PaymentProcess.create(
            paymentProcessId,
            accountId,
            command.amount(),
            command.paymentMethod()
        );
        apply(event, MetaData.with("processId", event.paymentProcessId().value()));
    }


    @CommandHandler
    public void handle(CompletePaymentProcessCommand command) {
        var event = paymentProcess.complete();
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(PaymentProcessEvent event) {
        if (paymentProcess != null) {
            paymentProcess = paymentProcess.applyEvent(event);
        } else {
            if (event instanceof PaymentProcessAcceptedEvent started) {
                paymentProcess = PaymentProcess.applyEvent(started);
            } else {
                throw new IllegalStateException("Expected PaymentProcessAcceptedEvent");
            }
        }
    }
}
