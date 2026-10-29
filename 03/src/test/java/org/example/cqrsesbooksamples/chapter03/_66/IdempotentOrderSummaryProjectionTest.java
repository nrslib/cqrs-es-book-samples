package org.example.cqrsesbooksamples.chapter03._66;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class IdempotentOrderSummaryProjectionTest {
    @Test
    void 処理済みイベントの記録によって集計の二重計上を防げる() {
        var summaryStore = new InMemoryOrderSummaryRepository();
        var processedStore = new InMemoryProcessedEventRepository();
        var projection = new IdempotentOrderSummaryProjection(summaryStore, processedStore);
        var event = new OrderPlacedEvent("event-1", "order-1", "2026-06", new BigDecimal("1000"));

        projection.on(event);
        projection.on(event);  // 重複配信を模す

        var summary = summaryStore.findByMonth("2026-06");
        assertThat(summary.getOrderCount()).isEqualTo(1);
        assertThat(summary.getTotalAmount()).isEqualByComparingTo("1000");
    }

    private static class InMemoryOrderSummaryRepository implements OrderSummaryRepository {
        private final Map<String, OrderSummary> summaries = new HashMap<>();

        @Override
        public OrderSummary findByMonth(String month) {
            return summaries.computeIfAbsent(month, OrderSummary::new);
        }

        @Override
        public OrderSummary save(OrderSummary summary) {
            summaries.put(summary.getMonth(), summary);
            return summary;
        }
    }

    private static class InMemoryProcessedEventRepository implements ProcessedEventRepository {
        private final Set<String> processed = new HashSet<>();

        @Override
        public boolean existsByEventId(String eventId) {
            return processed.contains(eventId);
        }

        @Override
        public ProcessedEvent save(ProcessedEvent processedEvent) {
            processed.add(processedEvent.eventId());
            return processedEvent;
        }
    }
}
