// 紙面照合: 3.50
package org.example.cqrsesbooksamples.chapter03._67;

import org.axonframework.eventhandling.EventHandler;

public class OrderNotificationHandler {
    private final ExternalApiClient externalApiClient;

    public OrderNotificationHandler(ExternalApiClient externalApiClient) {
        this.externalApiClient = externalApiClient;
    }


    @EventHandler
    public void on(OrderPlacedEvent event) {
        // べき等性キーを使用して外部APIを呼び出す
        externalApiClient.notifyOrder(
            event.orderId(),  // べき等性キー
            event.customerEmail(),
            event.amount()
        );
    }
}
