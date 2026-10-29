// 紙面照合: 7.5
package org.example.cqrsesbooksamples.chapter07._06;

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


@Aggregate
public class PaymentProcessAggregateAdapter {
    @AggregateIdentifier
    private PaymentProcessId paymentProcessId;

    protected PaymentProcessAggregateAdapter() {
    }

    @CommandHandler
    public PaymentProcessAggregateAdapter(StartPaymentCommand command) {
        var accountId = new AccountId(command.accountId());
        // 呼び出し元が採番済みなら引き継ぎ、なければ集約が採番する
        var paymentProcessId = command.processId() == null
            ? new PaymentProcessId()
            : new PaymentProcessId(command.processId().toString());
        var event = new PaymentProcessAcceptedEvent(
            paymentProcessId,
            accountId,
            command.amount(),
            command.paymentMethod()
        );
        // イベント発行時にプロセスIDをメタデータとして付与
        apply(event, MetaData.with("processId", event.paymentProcessId().value()));
    }

    @EventSourcingHandler
    public void on(PaymentProcessAcceptedEvent event) {
        this.paymentProcessId = event.paymentProcessId();
    }
}
