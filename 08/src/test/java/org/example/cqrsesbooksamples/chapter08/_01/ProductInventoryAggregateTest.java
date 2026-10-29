// 紙面照合: 8.1, 8.2, 8.3, 8.4
package org.example.cqrsesbooksamples.chapter08._01;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class ProductInventoryAggregateTest {
    private AggregateTestFixture<ProductInventoryAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(ProductInventoryAggregate.class);
    }


    @Test
    void shouldHandleAddInventoryCommand() {
        fixture.givenNoPriorActivity()
               .when(new AddInventoryCommand("PROD-001", 10))
               .expectEvents(new InventoryAddedEvent("PROD-001", 10));
    }


    @Test
    void shouldRejectNegativeQuantity() {
        fixture.givenNoPriorActivity()
               .when(new AddInventoryCommand("PROD-001", -5))
               .expectException(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNegativeRemovalQuantity() {
        fixture.given(new InventoryAddedEvent("PROD-001", 10))
               .when(new RemoveInventoryCommand("PROD-001", -5))
               .expectException(IllegalArgumentException.class)
               .expectNoEvents();
    }


    @Test
    void shouldUpdateInventoryState() {
        fixture.given(new InventoryAddedEvent("PROD-001", 10))
               .when(new RemoveInventoryCommand("PROD-001", 5))
               .expectEvents(new InventoryRemovedEvent("PROD-001", 5))
               .expectState(state -> {
                   assertEquals(5, state.getQuantity());
                   assertEquals("PROD-001", state.getProductId());
               });
    }


    @Test
    void shouldHandleComplexEventHistory() {
        fixture.given(
                new InventoryAddedEvent("PROD-001", 10),
                new InventoryRemovedEvent("PROD-001", 3),
                new InventoryAddedEvent("PROD-001", 5)
            )
            .when(new RemoveInventoryCommand("PROD-001", 2))
            .expectEvents(new InventoryRemovedEvent("PROD-001", 2))
            .expectState(state -> {
                assertEquals(10, state.getQuantity());
            });
    }
}
