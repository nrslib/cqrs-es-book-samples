// 検証補助: 2.57, 2.58
package org.example.cqrsesbooksamples.chapter02._73;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// eventGatewayで発行する素のイベントは集約IDを持たず、セグメントへの振り分けが
// メッセージID基準になる。複数セグメントだとリプレイ時にuser-1の登録と更新の
// 処理順が入れ替わりうるため、このテストは1セグメントで動かす
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter02_73;DB_CLOSE_DELAY=-1",
        "axon.eventhandling.processors.user-projection.mode=pooled",
        "axon.eventhandling.processors.user-projection.initial-segment-count=1"
})
class RebuildIntegrationTest {
    @Autowired
    private EventGateway eventGateway;

    @Autowired
    private UserReadModelRepository repository;

    @Autowired
    private ProjectionManagementService service;


    @Test
    void リードモデルを壊してもリビルドで再構築できる() {
        eventGateway.publish(new UserRegisteredEvent("user-1", "John", "john@example.com"));
        eventGateway.publish(new UserRegisteredEvent("user-2", "Jane", "jane@example.com"));
        eventGateway.publish(new UserUpdatedEvent("user-1", "John Doe", "john.doe@example.com"));

        // プロジェクションが追いつくのを待つ
        await().untilAsserted(() -> {
            assertThat(repository.count()).isEqualTo(2);
            assertThat(repository.findById("user-1").orElseThrow().getName()).isEqualTo("John Doe");
        });

        // リードモデルを意図的に破壊する
        var corrupted = repository.findById("user-1").orElseThrow();
        corrupted.updateName("CORRUPTED");
        repository.save(corrupted);
        assertThat(repository.findById("user-1").orElseThrow().getName()).isEqualTo("CORRUPTED");

        // トークンをリセットしてイベントを最初から再生する
        service.resetUserProjection();

        // イベントの再生によって正しい状態に再構築される
        await().untilAsserted(() -> {
            assertThat(repository.count()).isEqualTo(2);
            assertThat(repository.findById("user-1").orElseThrow().getName()).isEqualTo("John Doe");
            assertThat(repository.findById("user-2").orElseThrow().getName()).isEqualTo("Jane");
        });
    }
}
