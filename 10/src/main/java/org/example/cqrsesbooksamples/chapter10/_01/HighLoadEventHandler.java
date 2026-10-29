package org.example.cqrsesbooksamples.chapter10._01;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

// 削除済み（旧 10.1）
@Component
public class HighLoadEventHandler {
    @EventHandler
    public void on(OrderCreatedEvent event) {
        // 負荷に応じてインスタンス数を増減可能
        // 負荷増大時：スケールアウトで処理能力向上
        // 負荷減少時：スケールインでコスト最適化
    }
}
