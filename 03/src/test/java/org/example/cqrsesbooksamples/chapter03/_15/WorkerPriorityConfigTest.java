package org.example.cqrsesbooksamples.chapter03._15;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * リソースを配慮したワーカープール設定（旧 3.15）の検証。
 * 低優先度のワーカースレッドが指定どおりの名前と優先度で動くことを確かめる。
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter03_15;DB_CLOSE_DELAY=-1")
class WorkerPriorityConfigTest {
    @Autowired
    private EventProcessingConfiguration config;

    @Test
    void ワーカースレッドは低優先度で起動する() {
        // プロセッサが起動していることを確認する
        config.eventProcessor("user-projection", PooledStreamingEventProcessor.class).orElseThrow();

        // workerExecutor に指定したファクトリ由来のスレッドが最低優先度で動いている
        await().untilAsserted(() -> {
            var worker = Thread.getAllStackTraces().keySet().stream()
                    .filter(t -> t.getName().startsWith("user-projection-worker"))
                    .findFirst();
            assertThat(worker).isPresent();
            assertThat(worker.orElseThrow().getPriority()).isEqualTo(Thread.MIN_PRIORITY);
        });
    }
}
