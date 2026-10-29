// 検証補助: 3.8
package org.example.cqrsesbooksamples.chapter03._12;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * プロセッサの明示的な構成（リスト3.8）の検証。
 * registerPooledStreamingEventProcessor による登録で、
 * 指定した初期セグメント数のPooledプロセッサが起動することを確かめる。
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter03_12;DB_CLOSE_DELAY=-1")
class PooledProcessorRegistrationConfigTest {
    @Autowired
    private EventProcessingConfiguration config;

    @Test
    void 初期セグメント数4のPooledプロセッサとして起動する() {
        var processor = config.eventProcessor(
                "user-projection", PooledStreamingEventProcessor.class).orElseThrow();

        await().untilAsserted(() ->
                assertThat(processor.processingStatus()).hasSize(4));
    }
}
