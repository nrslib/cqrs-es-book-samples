// 紙面照合: 8.16, 8.17
package org.example.cqrsesbooksamples.chapter08._15;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter08_15;DB_CLOSE_DELAY=-1")
public class OrderSummaryPersistenceTest {
    @Autowired
    private OrderSummaryRepository repository;

    private OrderSummaryProjection projection;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        projection = new OrderSummaryProjection(repository);
    }


    @Test
    void shouldPersistProjectionState() {
        projection.on(new OrderCreatedEvent("ORDER-001", 1000));

        var summary = repository.findById("ORDER-001").orElseThrow();
        assertEquals(OrderStatus.CREATED, summary.getStatus());
        assertEquals(1000, summary.getAmount());
    }


    @Test
    void shouldMaintainConsistency() {
        projection.on(new OrderCreatedEvent("ORDER-001", 1000));
        projection.on(new PaymentCompletedEvent("ORDER-001"));
        projection.on(new OrderShippedEvent("ORDER-001"));

        var summary = repository.findById("ORDER-001").orElseThrow();
        assertEquals(OrderStatus.SHIPPED, summary.getStatus());
        assertEquals(1000, summary.getAmount());
        assertTrue(summary.isPaymentCompleted());
    }
}
