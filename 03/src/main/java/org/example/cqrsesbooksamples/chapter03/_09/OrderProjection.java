// 検証補助: 3.5
package org.example.cqrsesbooksamples.chapter03._09;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.example.cqrsesbooksamples.chapter03._08.support.InventoryRepository;
import org.example.cqrsesbooksamples.chapter03._08.support.OrderPlacedEvent;
import org.example.cqrsesbooksamples.chapter03._08.support.OrderRepository;
import org.example.cqrsesbooksamples.chapter03._08.support.OrderSummary;


// 正しい例1: 単一のEventHandler内で全ての処理を行う
@ProcessingGroup("order-projection")
public class OrderProjection {
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;

    public OrderProjection(OrderRepository orderRepository, InventoryRepository inventoryRepository) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @EventHandler
    public void on(OrderPlacedEvent event) {
        // 同じトランザクション内で実行される
        orderRepository.save(new OrderSummary(event.getOrderId()));
        inventoryRepository.decreaseStock(event.getProductId());
    }
}
