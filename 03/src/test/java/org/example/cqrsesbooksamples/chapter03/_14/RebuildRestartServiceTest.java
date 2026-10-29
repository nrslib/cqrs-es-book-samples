// 検証補助: 3.9
package org.example.cqrsesbooksamples.chapter03._14;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter03_14;DB_CLOSE_DELAY=-1",
        "axon.eventhandling.processors.user-projection.mode=pooled",
        "axon.eventhandling.processors.user-projection.initial-segment-count=1"
})

class RebuildRestartServiceTest {
    @Autowired
    private EventGateway eventGateway;

    @Autowired
    private RebuildRestartService service;

    @Autowired
    private CountingUserProjection projection;

    @Test
    void 指定時点からイベントを再生できる() {
        var boundary = Instant.parse("2026-01-01T00:00:00Z");
        publish("before", boundary.minusSeconds(1));
        publish("at", boundary);
        publish("after", boundary.plusSeconds(1));
        publish("last", boundary.plusSeconds(2));

        await().untilAsserted(() -> assertThat(projection.handledUserIds())
            .containsExactly("before", "at", "after", "last"));

        service.restartFromTimestamp(boundary);

        await().untilAsserted(() -> assertThat(projection.handledUserIds())
            .containsExactly("before", "at", "after", "last", "at", "after", "last"));
    }

    private void publish(String userId, Instant timestamp) {
        eventGateway.publish(new GenericEventMessage<>(UUID.randomUUID().toString(),
            new UserRegisteredEvent(userId, userId), Map.of(), timestamp));
    }
}
