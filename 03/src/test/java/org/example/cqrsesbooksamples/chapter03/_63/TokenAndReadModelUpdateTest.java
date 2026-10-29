package org.example.cqrsesbooksamples.chapter03._63;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * リードモデルとトークンの同時更新（旧 3.61）の検証。
 * Pooledプロセッサでイベントを処理すると、リードモデルの保存とあわせて
 * トラッキングトークンが前進することを確かめる。
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter03_63;DB_CLOSE_DELAY=-1",
        "axon.eventhandling.processors.user-projection.mode=pooled",
        "axon.eventhandling.processors.user-projection.initial-segment-count=1"
})
class TokenAndReadModelUpdateTest {
    @Autowired
    private EventGateway eventGateway;

    @Autowired
    private UserReadModelRepository repository;

    @Autowired
    private EventProcessingConfiguration config;

    @Test
    void リードモデルの保存とトークンの前進が両方行われる() {
        eventGateway.publish(new UserRegisteredEvent("user-1", "John", "john@example.com"));

        var processor = config.eventProcessor(
                "user-projection", PooledStreamingEventProcessor.class).orElseThrow();

        await().untilAsserted(() -> {
            // リードモデルが更新されている
            assertThat(repository.findById("user-1")).isPresent();
            // 同じ処理の中でトラッキングトークンも前進している
            var status = processor.processingStatus().get(0);
            assertThat(status).isNotNull();
            assertThat(status.getCurrentPosition().orElse(0)).isGreaterThanOrEqualTo(1);
        });

        var saved = repository.findById("user-1").orElseThrow();
        assertThat(saved.getName()).isEqualTo("John");
        assertThat(saved.getEmail()).isEqualTo("john@example.com");
    }
}
