package org.example.cqrsesbooksamples.chapter03._01;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter03_01;DB_CLOSE_DELAY=-1")
class SnapshotIntegrationTest {
    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void イベント100件ごとにスナップショットが作成される() {
        var productId = UUID.randomUUID().toString();
        commandGateway.sendAndWait(new CreateProductCommand(productId, "人気製品", new BigDecimal("1000"), 500));

        // 在庫更新イベントを積み上げて閾値（100イベント）を超えさせる
        for (int i = 0; i < 100; i++) {
            commandGateway.sendAndWait(new UpdateStockCommand(productId, -1));
        }

        // スナップショットは非同期に作成される
        await().untilAsserted(() -> {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM snapshot_event_entry WHERE aggregate_identifier = ?",
                Integer.class, productId);
            assertThat(count).isGreaterThanOrEqualTo(1);
        });

        // スナップショットからの復元後もコマンドを処理できる
        commandGateway.sendAndWait(new UpdateStockCommand(productId, 10));
    }
}
