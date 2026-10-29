package org.example.cqrsesbooksamples.chapter03._61;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.example.cqrsesbooksamples.chapter03._62.DeadLetterQueueController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter03_61;DB_CLOSE_DELAY=-1",
        "axon.eventhandling.processors.user-projection.mode=pooled"
})
class DeadLetterQueueIntegrationTest {
    @Autowired
    private EventGateway eventGateway;

    @Autowired
    private FlakyUserProjection projection;

    @Autowired
    private UserReadModelRepository repository;

    @Autowired
    private EventProcessingConfiguration config;

    @Autowired
    private DeadLetterQueueController controller;

    // 削除済み（旧 3.60）: DLQ再処理は1回の呼び出しで完了し、重複処理は発生しない
    @Test
    void 失敗イベントはDLQへ退避され修正後に再処理できる() {
        // プロジェクションを故障状態にしてイベントを流す
        projection.setFailing(true);
        eventGateway.publish(new UserRegisteredEvent("user-1", "John"));

        var queue = config.deadLetterQueue("user-projection").orElseThrow();
        await().untilAsserted(() ->
            assertThat(queue.amountOfSequences()).isEqualTo(1));

        // この時点でリードモデルは構築されていない
        assertThat(repository.count()).isZero();

        // 不具合を修正してデッドレターを再処理する
        projection.setFailing(false);
        await().atMost(java.time.Duration.ofSeconds(60)).untilAsserted(() -> {
            var response = controller.processAllDeadLetters("user-projection");
            assertThat(response.getBody()).containsEntry("processedSequences", 1);
        });

        // 再処理によってリードモデルが構築され、DLQは空になる
        assertThat(repository.findById("user-1")).isPresent();
        assertThat(queue.amountOfSequences()).isZero();
    }
}
