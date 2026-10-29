package org.example.cqrsesbooksamples.chapter10._01;

import org.axonframework.eventhandling.EventHandler;

// 削除済み（旧 10.2）
// 異なる集約からのイベントは順序が保証されない
public class CrossAggregateEventHandler {
    @EventHandler
    public void handle(OrderCreatedEvent event) {
        // このイベントと
    }

    @EventHandler
    public void handle(PaymentProcessedEvent event) {
        // このイベントの順序は
        // 必ずしも発生順と一致しない
    }
}
