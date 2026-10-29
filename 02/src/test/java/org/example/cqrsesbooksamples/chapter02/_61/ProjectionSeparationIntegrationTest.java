// 検証補助: 2.48
package org.example.cqrsesbooksamples.chapter02._61;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.StreamingEventProcessor;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 集約単位で分けたプロジェクション（リスト2.48）の検証。
 * ユーザと注文のプロジェクションが別グループの別プロセッサとして動き、
 * それぞれのリードモデルを更新することを確かめる。
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter02_61;DB_CLOSE_DELAY=-1",
        "axon.eventhandling.processors.user-projection.mode=pooled",
        "axon.eventhandling.processors.user-projection.initial-segment-count=1",
        "axon.eventhandling.processors.order-projection.mode=pooled",
        "axon.eventhandling.processors.order-projection.initial-segment-count=1"
})
class ProjectionSeparationIntegrationTest {
    @Autowired
    private EventGateway eventGateway;

    @Autowired
    private UserReadModelRepository userRepository;

    @Autowired
    private OrderReadModelRepository orderRepository;

    @Autowired
    private EventProcessingConfiguration config;

    @Test
    void 別グループのプロジェクションがそれぞれのリードモデルを更新する() {
        eventGateway.publish(new UserRegisteredEvent("user-1", "John"));
        eventGateway.publish(new OrderPlacedEvent("order-1", 1200));

        await().untilAsserted(() -> {
            assertThat(userRepository.count()).isEqualTo(1);
            assertThat(orderRepository.count()).isEqualTo(1);
        });
        assertThat(userRepository.findById("user-1").orElseThrow().getName()).isEqualTo("John");
        assertThat(orderRepository.findById("order-1").orElseThrow().getAmount()).isEqualTo(1200);

        // グループごとに独立したプロセッサが割り当てられている
        var userProcessor = config.eventProcessorByProcessingGroup(
                "user-projection", StreamingEventProcessor.class).orElseThrow();
        var orderProcessor = config.eventProcessorByProcessingGroup(
                "order-projection", StreamingEventProcessor.class).orElseThrow();
        assertThat(userProcessor).isNotSameAs(orderProcessor);
    }
}
