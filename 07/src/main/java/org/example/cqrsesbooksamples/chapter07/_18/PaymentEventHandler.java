package org.example.cqrsesbooksamples.chapter07._18;

import org.axonframework.eventhandling.EventHandler;
import org.example.cqrsesbooksamples.chapter07._16.OrderCreatedEvent;
import org.springframework.stereotype.Component;

// 削除済み（旧 7.18）
// 避けるべき実装：決済システムが注文システムのイベントをハンドリング
@Component
public class PaymentEventHandler {
    @EventHandler
    public void on(OrderCreatedEvent event) {  // 注文システムのイベントに依存
        // 処理
    }
}
