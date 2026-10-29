package org.example.cqrsesbooksamples.chapter03._45;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.StreamingEventProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter03_45;DB_CLOSE_DELAY=-1",
        "axon.eventhandling.processors.user-projection.mode=pooled",
        "axon.eventhandling.processors.user-projection.initial-segment-count=2",
        "axon.eventhandling.processors.user-projection.thread-count=4"
})
class SegmentManagementControllerTest {
    @Autowired
    private SegmentManagementController controller;

    @Autowired
    private EventProcessingConfiguration config;

    @Test
    void エンドポイント経由でセグメントを分割しマージして解放できる() {
        var processor = config.eventProcessor("user-projection", StreamingEventProcessor.class).orElseThrow();
        await().untilAsserted(() -> assertThat(processor.processingStatus()).hasSize(2));

        // 分割: 2 → 3 セグメント
        var splitResponse = controller.splitSegment("user-projection", 0).join();
        assertThat(splitResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        await().untilAsserted(() -> assertThat(processor.processingStatus()).hasSize(3));

        // マージ: 3 → 2 セグメント
        var mergeResponse = controller.mergeSegment("user-projection", 0).join();
        assertThat(mergeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        await().untilAsserted(() -> assertThat(processor.processingStatus()).hasSize(2));

        // 存在しないグループは404
        var notFound = controller.splitSegment("unknown-group", 0).join();
        assertThat(notFound.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // 所有中のセグメントを明示的に解放できる
        var releaseResponse = controller.releaseSegment("user-projection", 0);
        assertThat(releaseResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
