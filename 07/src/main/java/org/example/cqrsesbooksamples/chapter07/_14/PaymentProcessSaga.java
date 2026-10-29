// 紙面照合: 7.12
package org.example.cqrsesbooksamples.chapter07._14;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.example.cqrsesbooksamples.chapter07._01.CreditAcceptedEvent;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessAcceptedEvent;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessCompletedEvent;
import org.example.cqrsesbooksamples.chapter07.support.CancelPaymentProcessCommand;
import org.example.cqrsesbooksamples.chapter07.support.CompletePaymentProcessCommand;
import org.example.cqrsesbooksamples.chapter07.support.ExecuteCreditPaymentCommand;
import org.example.cqrsesbooksamples.chapter07.support.CreditRejectedEvent;
import org.example.cqrsesbooksamples.chapter07.support.NotificationPaymentExecutionSentEvent;
import org.example.cqrsesbooksamples.chapter07.support.SendPaymentCompletionNotificationCommand;
import org.example.cqrsesbooksamples.chapter07.support.PaymentMethod;
import org.example.cqrsesbooksamples.chapter07.support.PaymentProcessId;
import org.example.cqrsesbooksamples.chapter07.support.RequestBankTransferCommand;

import jakarta.inject.Inject;


@Saga
public class PaymentProcessSaga {
    @Inject
    private transient CommandGateway commandGateway;
    private PaymentProcessId processId;

    @StartSaga
    @SagaEventHandler(associationProperty = "paymentProcessId")
    public void on(PaymentProcessAcceptedEvent event) {
        this.processId = event.paymentProcessId();
        if (event.paymentMethod() == PaymentMethod.CREDIT) {
            commandGateway.send(new ExecuteCreditPaymentCommand(
                processId, event.accountId(), event.amount()));
        } else {
            commandGateway.send(new RequestBankTransferCommand(
                processId, event.accountId(), event.amount()));
        }
    }

    @SagaEventHandler(associationProperty = "paymentProcessId")
    public void on(CreditAcceptedEvent event) {
        commandGateway.send(
            new CompletePaymentProcessCommand(processId));
    }

    @SagaEventHandler(associationProperty = "paymentProcessId")
    public void on(PaymentProcessCompletedEvent event) {
        commandGateway.send(
            new SendPaymentCompletionNotificationCommand(processId));
    }

    @SagaEventHandler(associationProperty = "paymentProcessId")
    public void on(NotificationPaymentExecutionSentEvent event) {
        SagaLifecycle.end();
    }

    @SagaEventHandler(associationProperty = "paymentProcessId")
    public void on(CreditRejectedEvent event) {
        commandGateway.send(new CancelPaymentProcessCommand(
            processId, "クレジットカード決済が拒否されました"));
        SagaLifecycle.end();
    }
}
