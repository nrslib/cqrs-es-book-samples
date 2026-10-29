package org.example.cqrsesbooksamples.chapter03._13;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.StreamingEventProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter03_13;DB_CLOSE_DELAY=-1",
        "axon.eventhandling.processors.userProjection.mode=pooled",
        "axon.eventhandling.processors.userProjection.initial-segment-count=2",
        "axon.eventhandling.processors.userProjection.thread-count=4"
})
class ProcessorScalingServiceTest {
    @Autowired
    private ProcessorScalingService service;

    @Autowired
    private EventProcessingConfiguration config;

    @Test
    void 稼働中のプロセッサのセグメントを分割できる() {
        var processor = config.eventProcessor("userProjection", StreamingEventProcessor.class).orElseThrow();

        // 2セグメントがクレームされるのを待つ
        await().untilAsserted(() ->
            assertThat(processor.processingStatus()).hasSize(2));

        Boolean success = service.splitSegment("userProjection", 0).join();
        assertThat(success).isTrue();

        // 分割後は3セグメントになる
        await().untilAsserted(() ->
            assertThat(processor.processingStatus()).hasSize(3));
    }
}
