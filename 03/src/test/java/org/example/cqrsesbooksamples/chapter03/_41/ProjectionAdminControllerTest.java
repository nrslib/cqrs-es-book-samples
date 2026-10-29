package org.example.cqrsesbooksamples.chapter03._41;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter03_41;DB_CLOSE_DELAY=-1",
        "axon.eventhandling.processors.user-projection.mode=pooled",
        "axon.eventhandling.processors.user-projection.initial-segment-count=1"
})
class ProjectionAdminControllerTest {
    @Autowired
    private EventGateway eventGateway;

    @Autowired
    private EventStore eventStore;

    @Autowired
    private ProjectionAdminController controller;

    @Autowired
    private CountingProjection projection;

    @Test
    void 管理エンドポイントからリビルドを実行できる() {
        eventGateway.publish(new UserRegisteredEvent("before", "John"));
        eventGateway.publish(new UserRegisteredEvent("at", "Jane"));
        var position = eventStore.createHeadToken().position().orElseThrow();
        eventGateway.publish(new UserRegisteredEvent("after", "Alice"));
        eventGateway.publish(new UserRegisteredEvent("last", "Bob"));

        await().untilAsserted(() -> assertThat(projection.handledUserIds())
            .containsExactly("before", "at", "after", "last"));

        // トークンの位置は処理済みなので、その次のイベントから再処理される
        var positionResponse = controller.resetProcessorToPosition("user-projection", position);
        assertThat(positionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        await().untilAsserted(() -> assertThat(projection.handledUserIds())
            .containsExactly("before", "at", "after", "last", "after", "last"));

        // 存在しないグループは404
        assertThat(controller.resetProcessorToPosition("unknown-group", 1).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
