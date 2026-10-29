// 検証補助: 2.54
package org.example.cqrsesbooksamples.chapter02._68;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter02_68;DB_CLOSE_DELAY=-1")
class SegmentConfigIntegrationTest {
    @Autowired
    private EventProcessingConfiguration config;


    @Test
    void userProjectionは4セグメントのPooledStreamingEventProcessorで動く() {
        var processor = config.eventProcessor("user-projection", PooledStreamingEventProcessor.class);
        assertThat(processor).isPresent();

        // 4セグメントすべてが起動して処理状態に現れる
        await().untilAsserted(() ->
            assertThat(processor.orElseThrow().processingStatus()).hasSize(4));
    }
}
