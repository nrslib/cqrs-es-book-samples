// 紙面照合: 7.14
package org.example.cqrsesbooksamples.chapter07._16;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Service;


// 悪い例：決済システムが注文システムに依存している
@Service
public class PaymentService {
    // 注文システムのイベントに依存している
    @EventHandler
    public void on(OrderCreatedEvent event) {
        // 注文固有の処理ロジックが決済システムに混入
        if (event.getOrderType() == OrderType.SUBSCRIPTION) {
            // サブスクリプション注文の処理
        } else {
            // 通常注文の処理
        }
    }

    @EventHandler
    public void on(OrderCancelledEvent event) {
        // 注文キャンセル時の処理
    }
}
