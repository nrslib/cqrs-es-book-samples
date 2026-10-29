package org.example.cqrsesbooksamples.chapter03._04;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Instant;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter03_04;DB_CLOSE_DELAY=-1",
        "projection.active=v2"
})
class BlueGreenProjectionTest {
    @Autowired
    private EventGateway eventGateway;

    @Autowired
    private QueryGateway queryGateway;

    @Autowired
    private UserReadModelV1Repository v1Repository;

    @Autowired
    private UserReadModelV2Repository v2Repository;

    @Test
    void 新旧プロジェクションが並行稼働しクエリはv2へルーティングされる() {
        eventGateway.publish(new UserRegisteredEvent(
            "user-1", "Taro", "Yamada Taro", "taro@example.com", Instant.parse("2026-01-01T00:00:00Z")));

        // 両方のプロセッサグループが独立してリードモデルを構築する
        await().untilAsserted(() -> {
            assertThat(v1Repository.findById("user-1")).isPresent();
            assertThat(v2Repository.findById("user-1")).isPresent();
        });

        // projection.active=v2 のため、クエリはv2のリードモデルから応答される
        var summary = queryGateway.query(new GetUserQuery("user-1"), UserSummary.class).join();
        assertThat(summary.displayName()).isEqualTo("Yamada Taro");
    }
}
