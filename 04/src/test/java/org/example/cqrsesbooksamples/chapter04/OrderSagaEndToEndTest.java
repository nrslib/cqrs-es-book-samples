// 検証補助: 4.25, 4.27
package org.example.cqrsesbooksamples.chapter04;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.util.List;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.example.cqrsesbooksamples.chapter04._20.AddInventoryCommand;
import org.example.cqrsesbooksamples.chapter04._20.CreateProductCommand;
import org.example.cqrsesbooksamples.chapter04._20.InventoryId;
import org.example.cqrsesbooksamples.chapter04._20.InventoryReservedEvent;
import org.example.cqrsesbooksamples.chapter04._29.CreateOrderCommand;
import org.example.cqrsesbooksamples.chapter04._29.OrderCancelledEvent;
import org.example.cqrsesbooksamples.chapter04._29.OrderCompletedEvent;
import org.example.cqrsesbooksamples.chapter04._29.OrderCreatedEvent;
import org.example.cqrsesbooksamples.chapter04._29.OrderLine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter04_e2e;DB_CLOSE_DELAY=-1",
        "spring.main.allow-bean-definition-overriding=true"
})
class OrderSagaEndToEndTest {
    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private EventStore eventStore;


    @Test
    void 同一商品の複数明細は合計数量を確保して確定する() {
        String productId = commandGateway.sendAndWait(new CreateProductCommand("商品", new BigDecimal("1000")));
        commandGateway.sendAndWait(new AddInventoryCommand(productId, 10));
        String orderId = commandGateway.sendAndWait(new CreateOrderCommand(List.of(
            new OrderLine(productId, 2), new OrderLine(productId, 3))));

        await().untilAsserted(() -> assertThat(eventStore.readEvents(orderId).asStream()
            .map(message -> message.getPayload()).toList()).anyMatch(OrderCompletedEvent.class::isInstance));
        var inventoryEvents = eventStore.readEvents(new InventoryId(productId).toString())
            .asStream().map(message -> message.getPayload())
            .filter(InventoryReservedEvent.class::isInstance)
            .map(InventoryReservedEvent.class::cast).toList();
        assertThat(inventoryEvents).extracting(event -> event.quantity()).containsExactly(5);
        var created = (OrderCreatedEvent) eventStore.readEvents(orderId).next().getPayload();
        assertThat(created.orderLines()).containsExactly(new OrderLine(productId, 5));

        String nextOrderId = commandGateway.sendAndWait(new CreateOrderCommand(List.of(new OrderLine(productId, 7))));
        await().untilAsserted(() -> assertThat(eventStore.readEvents(nextOrderId).asStream()
            .map(message -> message.getPayload()).toList()).anyMatch(OrderCancelledEvent.class::isInstance));
    }

    @Test
    void 在庫が足りる注文はサガが予約を進めて確定する() {
        String productA = commandGateway.sendAndWait(new CreateProductCommand("商品A", new BigDecimal("1000")));
        String productB = commandGateway.sendAndWait(new CreateProductCommand("商品B", new BigDecimal("2000")));
        commandGateway.sendAndWait(new AddInventoryCommand(productA, 10));
        commandGateway.sendAndWait(new AddInventoryCommand(productB, 5));

        String orderId = commandGateway.sendAndWait(new CreateOrderCommand(List.of(
            new OrderLine(productA, 2),
            new OrderLine(productB, 1))));

        // サガが商品ごとの予約を進め、最終的に注文が確定される
        await().untilAsserted(() -> {
            var payloads = eventStore.readEvents(orderId).asStream()
                .map(message -> message.getPayload())
                .toList();
            assertThat(payloads).anySatisfy(payload ->
                assertThat(payload).isInstanceOf(OrderCompletedEvent.class));
        });
    }

    @Test
    void 在庫が不足する注文はサガが補償を実行してキャンセルされる() {
        String productA = commandGateway.sendAndWait(new CreateProductCommand("商品A", new BigDecimal("1000")));
        String productB = commandGateway.sendAndWait(new CreateProductCommand("商品B", new BigDecimal("2000")));
        commandGateway.sendAndWait(new AddInventoryCommand(productA, 10));
        commandGateway.sendAndWait(new AddInventoryCommand(productB, 1));

        String orderId = commandGateway.sendAndWait(new CreateOrderCommand(List.of(
            new OrderLine(productA, 2),
            new OrderLine(productB, 5))));

        // 商品Bの在庫不足を検知したサガが、商品Aの予約を解放して注文をキャンセルする
        await().untilAsserted(() -> {
            var payloads = eventStore.readEvents(orderId).asStream()
                .map(message -> message.getPayload())
                .toList();
            assertThat(payloads).anySatisfy(payload ->
                assertThat(payload).isInstanceOf(OrderCancelledEvent.class));
        });
    }
}
