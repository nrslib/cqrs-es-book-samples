package org.example.cqrsesbooksamples.chapter03._03;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter03_03;DB_CLOSE_DELAY=-1")
class CustomSnapshotTriggerIntegrationTest {
    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 価格変更イベントでスナップショットが予約される() {
        var productId = UUID.randomUUID().toString();
        commandGateway.sendAndWait(new CreateProductCommand(productId, "製品A", new BigDecimal("1000")));

        // 作成イベントだけではスナップショットは作られない
        Integer before = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM snapshot_event_entry WHERE aggregate_identifier = ?",
            Integer.class, productId);
        assertThat(before).isZero();

        // 節目となる価格変更イベントが発生するとスナップショットが予約される
        commandGateway.sendAndWait(new ChangePriceCommand(productId, new BigDecimal("1200")));

        await().untilAsserted(() -> {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM snapshot_event_entry WHERE aggregate_identifier = ?",
                Integer.class, productId);
            assertThat(count).isGreaterThanOrEqualTo(1);
        });
    }
}
