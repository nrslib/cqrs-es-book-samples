package org.example.cqrsesbooksamples.chapter04._32;

import java.util.List;

import org.axonframework.test.saga.SagaTestFixture;
import org.example.cqrsesbooksamples.chapter04._20.InventoryReservationFailedEvent;
import org.example.cqrsesbooksamples.chapter04._20.InventoryReservedEvent;
import org.example.cqrsesbooksamples.chapter04._20.ReleaseInventoryCommand;
import org.example.cqrsesbooksamples.chapter04._20.ReserveInventoryCommand;
import org.example.cqrsesbooksamples.chapter04._29.CancelOrderCommand;
import org.example.cqrsesbooksamples.chapter04._29.CompleteOrderCommand;
import org.example.cqrsesbooksamples.chapter04._29.OrderCreatedEvent;
import org.example.cqrsesbooksamples.chapter04._29.OrderLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderProcessingSagaTest {
    private SagaTestFixture<OrderProcessingSaga> fixture;

    @BeforeEach
    void setUp() {
        fixture = new SagaTestFixture<>(OrderProcessingSaga.class);
    }

    @Test
    void 注文作成で最初の商品の予約コマンドが送られる() {
        fixture.givenNoPriorActivity()
            .whenAggregate("order-1").publishes(new OrderCreatedEvent("order-1", List.of(
                new OrderLine("product-A", 2),
                new OrderLine("product-B", 1))))
            .expectActiveSagas(1)
            .expectDispatchedCommands(new ReserveInventoryCommand("product-A", 2, "order-1"));
    }

    @Test
    void 予約成功で次の商品の予約に進む() {
        fixture.givenAggregate("order-1").published(new OrderCreatedEvent("order-1", List.of(
                new OrderLine("product-A", 2),
                new OrderLine("product-B", 1))))
            .whenPublishingA(new InventoryReservedEvent("product-A", 2, "order-1"))
            .expectDispatchedCommands(new ReserveInventoryCommand("product-B", 1, "order-1"));
    }

    @Test
    void すべての予約が成功すると注文が確定されサガが終了する() {
        fixture.givenAggregate("order-1").published(
                new OrderCreatedEvent("order-1", List.of(new OrderLine("product-A", 2))))
            .whenPublishingA(new InventoryReservedEvent("product-A", 2, "order-1"))
            .expectDispatchedCommands(new CompleteOrderCommand("order-1"))
            .expectActiveSagas(0);
    }

    @Test
    void 途中で失敗すると確保済みの在庫を解放して注文をキャンセルする() {
        fixture.givenAggregate("order-1").published(
                new OrderCreatedEvent("order-1", List.of(
                    new OrderLine("product-A", 2),
                    new OrderLine("product-B", 1))),
                new InventoryReservedEvent("product-A", 2, "order-1"))
            .whenPublishingA(new InventoryReservationFailedEvent("product-B", "order-1"))
            .expectDispatchedCommands(
                new ReleaseInventoryCommand("product-A", "order-1"),
                new CancelOrderCommand("order-1"))
            .expectActiveSagas(0);
    }
}
