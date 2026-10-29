// 紙面照合: 4.28
package org.example.cqrsesbooksamples.chapter04._32;

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
    private List<OrderLine> remainingLines;
    private List<OrderLine> reservedLines;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderCreatedEvent event) {
        this.orderId = event.orderId();
        this.remainingLines = new ArrayList<>(event.orderLines());
        this.reservedLines = new ArrayList<>();

        // 最初の商品の在庫確保を試みる
        requestNextInventoryReservation();
    }

    private void requestNextInventoryReservation() {
        if (remainingLines.isEmpty()) {
            // すべての商品の在庫確保が完了
            commandGateway.send(
                new CompleteOrderCommand(orderId));
            SagaLifecycle.end();
            return;
        }

        var line = remainingLines.get(0);
        commandGateway.send(new ReserveInventoryCommand(
            line.productId(),
            line.quantity(),
            orderId
        ));
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(InventoryReservedEvent event) {
        // 成功した商品を記録
        var reservedLine = remainingLines.remove(0);
        reservedLines.add(reservedLine);

        // 次の商品の在庫確保を試みる
        requestNextInventoryReservation();
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(InventoryReservationFailedEvent event) {
        // いずれかの商品で在庫確保に失敗した場合、
        // すでに確保した在庫をすべて解放
        for (var line : reservedLines) {
            commandGateway.send(new ReleaseInventoryCommand(
                line.productId(),
                orderId
            ));
        }

        // 注文をキャンセル
        commandGateway.send(
            new CancelOrderCommand(orderId));
        SagaLifecycle.end();
    }
}
