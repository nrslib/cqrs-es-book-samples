package org.example.cqrsesbooksamples.chapter04._20;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InventoryAggregateAdapterTest {
    private AggregateTestFixture<InventoryAggregateAdapter> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(InventoryAggregateAdapter.class);
    }

    @Test
    void 初回の在庫追加で在庫が作られる() {
        fixture.givenNoPriorActivity()
            .when(new AddInventoryCommand("product-1", 10))
            .expectEvents(new InventoryCreatedEvent("product-1", 10));
    }

    @Test
    void 既存在庫への追加は加算イベントになる() {
        fixture.given(new InventoryCreatedEvent("product-1", 10))
            .when(new AddInventoryCommand("product-1", 5))
            .expectEvents(new InventoryAddedEvent("product-1", 5));
    }

    @Test
    void 在庫が足りれば予約イベントが発行される() {
        fixture.given(new InventoryCreatedEvent("product-1", 10))
            .when(new ReserveInventoryCommand("product-1", 3, "order-1"))
            .expectEvents(new InventoryReservedEvent("product-1", 3, "order-1"));
    }

    @Test
    void 予約済みを差し引いた残りが不足すると失敗イベントが発行される() {
        fixture.given(
                new InventoryCreatedEvent("product-1", 10),
                new InventoryReservedEvent("product-1", 8, "order-1"))
            .when(new ReserveInventoryCommand("product-1", 3, "order-2"))
            .expectEvents(new InventoryReservationFailedEvent("product-1", "order-2"));
    }

    @Test
    void 解放すると予約済み数量が戻る() {
        fixture.given(
                new InventoryCreatedEvent("product-1", 10),
                new InventoryReservedEvent("product-1", 8, "order-1"),
                new InventoryReleasedEvent("product-1", 8, "order-1"))
            .when(new ReserveInventoryCommand("product-1", 3, "order-2"))
            .expectEvents(new InventoryReservedEvent("product-1", 3, "order-2"));
    }

    @Test
    void 解放は予約の記録にある数量でイベントを発行する() {
        fixture.given(
                new InventoryCreatedEvent("product-1", 10),
                new InventoryReservedEvent("product-1", 8, "order-1"))
            .when(new ReleaseInventoryCommand("product-1", "order-1"))
            .expectEvents(new InventoryReleasedEvent("product-1", 8, "order-1"));
    }

    @Test
    void 二重に届いた解放は何も起こさない() {
        fixture.given(
                new InventoryCreatedEvent("product-1", 10),
                new InventoryReservedEvent("product-1", 8, "order-1"),
                new InventoryReleasedEvent("product-1", 8, "order-1"))
            .when(new ReleaseInventoryCommand("product-1", "order-1"))
            .expectSuccessfulHandlerExecution()
            .expectNoEvents();
    }

    @Test
    void 予約の記録がない注文への解放は何も起こさない() {
        fixture.given(
                new InventoryCreatedEvent("product-1", 10),
                new InventoryReservedEvent("product-1", 8, "order-1"))
            .when(new ReleaseInventoryCommand("product-1", "order-2"))
            .expectSuccessfulHandlerExecution()
            .expectNoEvents();
    }
}
