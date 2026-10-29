package org.example.cqrsesbooksamples.chapter03._44;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter03_44;DB_CLOSE_DELAY=-1")
class InitialSegmentCountTest {
    @Autowired
    private EventProcessingConfiguration config;

    @Test
    void 初期セグメント数4で起動する() {
        var processor = config.eventProcessor("user-projection", PooledStreamingEventProcessor.class).orElseThrow();
        await().untilAsserted(() ->
            assertThat(processor.processingStatus()).hasSize(4));
    }
}
