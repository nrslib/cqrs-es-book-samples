package org.example.cqrsesbooksamples.chapter08._11;

import java.time.Duration;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;

import jakarta.inject.Inject;

/**
 * 注文の作成から出荷までを取りまとめるサガ。
 * 支払いには30分の期限があり、超過すると注文をキャンセルする。
 */
@Saga
public class OrderProcessSaga {
    private static final Duration PAYMENT_TIMEOUT = Duration.ofMinutes(30);

    @Inject
    private transient CommandGateway commandGateway;

    @Inject
    private transient EventScheduler eventScheduler;

    private String orderId;
    private boolean paymentCompleted;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderCreatedEvent event) {
        this.orderId = event.orderId();
        commandGateway.send(new ProcessPaymentCommand(event.orderId(), event.amount()));
        eventScheduler.schedule(PAYMENT_TIMEOUT, new OrderPaymentDeadlineExpired(event.orderId()));
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(PaymentCompletedEvent event) {
        this.paymentCompleted = true;
        commandGateway.send(new ShipOrderCommand(event.orderId()));
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderShippedEvent event) {
        SagaLifecycle.end();
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(PaymentFailedEvent event) {
        commandGateway.send(new ReleaseInventoryCommand(event.orderId()));
        commandGateway.send(new NotifyCustomerOfFailureCommand(event.orderId()));
        SagaLifecycle.end();
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderPaymentDeadlineExpired event) {
        if (paymentCompleted) {
            return;
        }
        commandGateway.send(new CancelOrderCommand(event.orderId()));
        SagaLifecycle.end();
    }
}
