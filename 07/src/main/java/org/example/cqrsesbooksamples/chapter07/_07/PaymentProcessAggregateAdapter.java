package org.example.cqrsesbooksamples.chapter07._07;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessAcceptedEvent;
import org.example.cqrsesbooksamples.chapter07.support.AccountId;
import org.example.cqrsesbooksamples.chapter07.support.PaymentProcessId;
import org.example.cqrsesbooksamples.chapter07.support.StartPaymentCommand;

// 削除済み（旧 7.7）
@Aggregate
public class PaymentProcessAggregateAdapter {
    @AggregateIdentifier
    private PaymentProcessId paymentProcessId;

    protected PaymentProcessAggregateAdapter() {
    }

    @CommandHandler
    public PaymentProcessAggregateAdapter(StartPaymentCommand command, MetaData metaData) {
        var accountId = new AccountId(command.accountId());
        var paymentProcessId = command.processId() == null
            ? new PaymentProcessId()
            : new PaymentProcessId(command.processId().toString());
        var event = new PaymentProcessAcceptedEvent(
            paymentProcessId,
            accountId,
            command.amount(),
            command.paymentMethod()
        );
        apply(event, metaData);
    }

    @EventSourcingHandler
    public void on(PaymentProcessAcceptedEvent event) {
        this.paymentProcessId = event.paymentProcessId();
    }
}
