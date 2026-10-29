// 検証補助: 3.17, 3.18, 3.19
package org.example.cqrsesbooksamples.chapter03._51;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.StreamingEventProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter03_51;DB_CLOSE_DELAY=-1",
        "axon.eventhandling.processors.user-projection.mode=pooled",
        "axon.eventhandling.processors.user-projection.initial-segment-count=2",
        "axon.eventhandling.processors.user-projection.thread-count=4"
})
class BroadcastSegmentManagementTest {
    @Autowired
    private SegmentManagementController controller;

    @Autowired
    private EventProcessingConfiguration config;

    // ブロードキャスト呼び出しの前提となる、所有報告とNOT_OWNED応答の検証
    @Test
    void 所有セグメントの報告と分割とリセットを実行できる() {
        var processor = config.eventProcessor("user-projection", StreamingEventProcessor.class).orElseThrow();
        await().untilAsserted(() -> assertThat(processor.processingStatus()).hasSize(2));

        // 所有セグメント情報の報告（単一インスタンスのため全セグメントを所有）
        var ownership = controller.getSegmentOwnership("user-projection").getBody();
        assertThat(ownership).isNotNull();
        assertThat(ownership.ownedSegments()).hasSize(2);

        // 所有しているセグメントの分割は成功する
        var splitResult = controller.splitSegment("user-projection", 0).join().getBody();
        assertThat(splitResult).isNotNull();
        assertThat(splitResult.status()).isEqualTo(OperationStatus.SUCCESS);

        // 所有していないセグメントIDを指定すると NOT_OWNED が返る
        var notOwned = controller.splitSegment("user-projection", 99).join().getBody();
        assertThat(notOwned).isNotNull();
        assertThat(notOwned.status()).isEqualTo(OperationStatus.NOT_OWNED);

        // リビルドは「全台停止 → 1台でリセット → 全台再開」の順で行う
        assertThat(controller.stopProcessor("user-projection").getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(controller.resetProcessor("user-projection").getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(controller.startProcessor("user-projection").getStatusCode().is2xxSuccessful()).isTrue();

        // 再開後はセグメントを取り直して処理が続く
        await().untilAsserted(() -> assertThat(processor.processingStatus()).isNotEmpty());
    }
}
