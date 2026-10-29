// 紙面照合: 4.29
package org.example.cqrsesbooksamples.chapter04._33;

import java.util.ArrayList;
import java.util.List;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.example.cqrsesbooksamples.chapter04._20.InventoryReservationFailedEvent;
import org.example.cqrsesbooksamples.chapter04._20.InventoryReservedEvent;
import org.example.cqrsesbooksamples.chapter04._20.ReleaseInventoryCommand;
import org.example.cqrsesbooksamples.chapter04._20.ReserveInventoryCommand;
import org.example.cqrsesbooksamples.chapter04._29.CancelOrderCommand;
import org.example.cqrsesbooksamples.chapter04._29.CompleteOrderCommand;
import org.example.cqrsesbooksamples.chapter04._29.OrderCreatedEvent;
import org.example.cqrsesbooksamples.chapter04._29.OrderLine;

import jakarta.inject.Inject;


@Saga
public class OrderProcessingSaga {
    @Inject
    private transient CommandGateway commandGateway;
    private String orderId;
    private List<OrderLine> orderLines;
    private List<OrderLine> successfullyReservedLines = new ArrayList<>();
    private List<OrderLine> failedReservationLines = new ArrayList<>();
    private int pendingReservations;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderCreatedEvent event) {
        this.orderId = event.orderId();
        this.orderLines = new ArrayList<>(event.orderLines());
        this.pendingReservations = orderLines.size();

        // すべての商品の在庫確保を並列に試みる
        for (var line : orderLines) {
            commandGateway.send(new ReserveInventoryCommand(
                line.productId(),
                line.quantity(),
                orderId
            ));
        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(InventoryReservedEvent event) {
        successfullyReservedLines.add(
            orderLines.stream()
                .filter(line -> line.productId().equals(event.productId()))
                .findFirst()
                .orElseThrow()
        );
        checkReservationCompletion();
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(InventoryReservationFailedEvent event) {
        failedReservationLines.add(
            orderLines.stream()
                .filter(line -> line.productId().equals(event.productId()))
                .findFirst()
                .orElseThrow()
        );
        checkReservationCompletion();
    }

    private void checkReservationCompletion() {
        pendingReservations--;
        if (pendingReservations > 0) {
            return;  // まだ応答が揃っていない
        }
        if (failedReservationLines.isEmpty()) {
            completeOrder();
        } else {
            releaseAndCancel();
        }
    }

    // すべての在庫確保に成功したので注文を確定
    private void completeOrder() {
        commandGateway.send(new CompleteOrderCommand(orderId));
        SagaLifecycle.end();
    }

    // すでに確保できた分の在庫を解放して注文をキャンセル
    private void releaseAndCancel() {
        for (var line : successfullyReservedLines) {
            commandGateway.send(new ReleaseInventoryCommand(
                line.productId(),
                orderId
            ));
        }
        commandGateway.send(new CancelOrderCommand(orderId));
        SagaLifecycle.end();
    }
}
