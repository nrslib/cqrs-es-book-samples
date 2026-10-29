package org.example.cqrsesbooksamples.chapter04._20;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter04_20;DB_CLOSE_DELAY=-1")
class WarehouseIntegrationTest {
    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private InventoryQueryController controller;

    @Autowired
    private ProductInventoryViewRepository repository;

    @Autowired
    private EventProcessingConfiguration eventProcessingConfiguration;

    @Test
    void 製品と在庫のイベントが統合リードモデルに反映される() {
        assertThat(eventProcessingConfiguration.eventProcessor(
            "product-inventory", PooledStreamingEventProcessor.class)).isPresent();

        String productId = commandGateway.sendAndWait(
            new CreateProductCommand("ノートPC", new BigDecimal("150000")));

        commandGateway.sendAndWait(new AddInventoryCommand(productId, 10));
        commandGateway.sendAndWait(new AddInventoryCommand(productId, 5));

        await().untilAsserted(() -> {
            assertThat(repository.findById(productId)).isPresent();
            var response = controller.getInventory(productId);
            assertThat(response.name()).isEqualTo("ノートPC");
            assertThat(response.quantity()).isEqualTo(15);
        });
    }
}
