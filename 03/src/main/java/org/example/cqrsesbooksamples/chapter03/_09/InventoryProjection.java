// 紙面照合: 3.5
package org.example.cqrsesbooksamples.chapter03._09;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.example.cqrsesbooksamples.chapter03._08.support.InventoryRepository;
import org.example.cqrsesbooksamples.chapter03._08.support.OrderPlacedEvent;


@ProcessingGroup("order-projection")  // 同じグループ名を指定
public class InventoryProjection {
    private final InventoryRepository inventoryRepository;

    public InventoryProjection(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @EventHandler
    public void on(OrderPlacedEvent event) {
        // OrderSummaryProjectionと同じトランザクション内で順次実行される
        inventoryRepository.decreaseStock(event.getProductId());
    }
}
