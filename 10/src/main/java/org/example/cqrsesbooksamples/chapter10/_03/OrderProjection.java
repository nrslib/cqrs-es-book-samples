package org.example.cqrsesbooksamples.chapter10._03;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

// 削除済み（旧 10.3）
@Component
@ProcessingGroup("order-projection")
public class OrderProjection {

    @EventHandler
    public void on(OrderCreatedEvent event) {
        // 同一集約のイベントは同じシャードに割り当てられ
        // 必ず発生順に処理される
    }

    @EventHandler
    public void on(OrderUpdatedEvent event) {
        // このイベントは必ず、同じ注文の
        // OrderCreatedEventの後に処理される
    }
}
