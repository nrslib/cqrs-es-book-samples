// 紙面照合: 8.15
package org.example.cqrsesbooksamples.chapter08._15;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class OrderSummaryProjectionTest {
    private OrderSummaryProjection projection;
    private OrderSummaryRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(OrderSummaryRepository.class);
        projection = new OrderSummaryProjection(repository);
    }

    @Test
    void shouldUpdateOrderSummary() {
        var summary = new OrderSummary("ORDER-001");
        when(repository.findById("ORDER-001")).thenReturn(Optional.of(summary));

        projection.on(new OrderShippedEvent("ORDER-001"));

        verify(repository).save(argThat(s ->
            s.getStatus().equals(OrderStatus.SHIPPED)));
    }
}
