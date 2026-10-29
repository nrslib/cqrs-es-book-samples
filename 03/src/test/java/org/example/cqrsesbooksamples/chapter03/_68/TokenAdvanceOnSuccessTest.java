package org.example.cqrsesbooksamples.chapter03._68;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 失敗時はトークンが進まず同じイベントから再開され、成功時のみトークンが進むことを検証する。
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter03_68;DB_CLOSE_DELAY=-1",
        "axon.eventhandling.processors.retry-projection.mode=pooled"
})
class TokenAdvanceOnSuccessTest {
    @Autowired
    private EventGateway eventGateway;

    @Autowired
    private FlakyUserProjection projection;

    @Test
    void 処理が失敗した場合はトークンが進まず同じイベントから再開される() {
        eventGateway.publish(new UserRegisteredEvent("user-1", "John"));

        // 2回失敗したあと、3回目の配信で成功する（同じイベントが再配信されている）
        await().atMost(Duration.ofSeconds(60)).untilAsserted(() ->
            assertThat(projection.processedUserIds()).containsExactly("user-1"));
        assertThat(projection.attempts()).isGreaterThanOrEqualTo(3);

        // 成功してトークンが進んだあとは、同じイベントが再処理されない
        eventGateway.publish(new UserRegisteredEvent("user-2", "Jane"));
        await().atMost(Duration.ofSeconds(60)).untilAsserted(() ->
            assertThat(projection.processedUserIds()).containsExactly("user-1", "user-2"));
    }
}
