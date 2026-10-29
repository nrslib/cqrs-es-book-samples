package org.example.cqrsesbooksamples.chapter02._54;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.Test;

class OrderAggregateTest {
    // 削除済み（旧 2.54）
    @Test
    void 注文するとOrderPlacedEventが発行される() {
        var fixture = new AggregateTestFixture<>(OrderAggregate.class);
        fixture.givenNoPriorActivity()
            .when(new PlaceOrderCommand("order-123", 1000))
            .expectEvents(new OrderPlacedEvent("order-123", 1000));
    }
}
