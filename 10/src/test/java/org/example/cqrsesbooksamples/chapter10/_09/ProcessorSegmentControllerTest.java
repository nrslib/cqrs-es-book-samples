package org.example.cqrsesbooksamples.chapter10._09;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter10_09;DB_CLOSE_DELAY=-1",
        "axon.eventhandling.processors.sample-projection.mode=pooled",
        "axon.eventhandling.processors.sample-projection.initial-segment-count=2",
        "axon.eventhandling.processors.sample-projection.thread-count=4"
})
class ProcessorSegmentControllerTest {
    @Autowired
    private ProcessorSegmentController controller;

    @Autowired
    private EventProcessingConfiguration config;

    @Test
    void 運用中にセグメントを分割し統合できる() {
        var processor = config.eventProcessor("sample-projection", PooledStreamingEventProcessor.class).orElseThrow();
        await().untilAsserted(() -> assertThat(processor.processingStatus()).hasSize(2));

        controller.splitSegment("sample-projection", 0);
        await().untilAsserted(() -> assertThat(processor.processingStatus()).hasSize(3));

        controller.mergeSegment("sample-projection", 0);
        await().untilAsserted(() -> assertThat(processor.processingStatus()).hasSize(2));
    }

    @Test
    void 存在しないプロセッサ名では例外になる() {
        assertThatThrownBy(() -> controller.splitSegment("unknown", 0))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
