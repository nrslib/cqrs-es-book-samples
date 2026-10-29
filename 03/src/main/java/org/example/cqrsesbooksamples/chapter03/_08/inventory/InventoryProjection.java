package org.example.cqrsesbooksamples.chapter03._08.inventory;

import org.axonframework.eventhandling.EventHandler;
import org.example.cqrsesbooksamples.chapter03._08.support.InventoryRepository;
import org.example.cqrsesbooksamples.chapter03._08.support.OrderPlacedEvent;

// 削除済み（旧 3.8）
public class InventoryProjection {
    private final InventoryRepository inventoryRepository;

    public InventoryProjection(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @EventHandler
    public void on(OrderPlacedEvent event) {
        // 上記のOrderProjectionとは別のトランザクションで実行される
        inventoryRepository.decreaseStock(event.getProductId());
    }
}
