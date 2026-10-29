// 検証補助: 2.60
package org.example.cqrsesbooksamples.chapter02._76;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * リードモデル更新と外部通知のグループ分離（リスト2.60）と、
 * 片方だけのリセット（旧 2.77）の検証。
 * user-projection をリセットしてもリードモデルは再構築される一方、
 * user-notification は影響を受けず通知の再送が起きないことを確かめる。
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter02_76;DB_CLOSE_DELAY=-1",
        "axon.eventhandling.processors.user-projection.mode=pooled",
        "axon.eventhandling.processors.user-projection.initial-segment-count=1",
        "axon.eventhandling.processors.user-notification.mode=pooled",
        "axon.eventhandling.processors.user-notification.initial-segment-count=1"
})
class ProjectionResetScopeIntegrationTest {
    @Autowired
    private EventGateway eventGateway;

    @Autowired
    private UserReadModelRepository repository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ProjectionManagementService service;

    @Test
    void プロジェクションのリセットは通知グループに波及しない() {
        eventGateway.publish(new UserRegisteredEvent("user-1", "John", "john@example.com"));
        eventGateway.publish(new UserRegisteredEvent("user-2", "Jane", "jane@example.com"));

        // 両グループが追いつくのを待つ
        await().untilAsserted(() -> {
            assertThat(repository.count()).isEqualTo(2);
            assertThat(notificationService.sentEmails()).hasSize(2);
        });

        // user-projection だけリセットするとイベント再生でリードモデルが再構築される
        service.resetUserProjection();

        await().untilAsserted(() -> {
            assertThat(repository.count()).isEqualTo(2);
            assertThat(repository.findById("user-1").orElseThrow().getName()).isEqualTo("John");
        });

        // user-notification グループのトークンはそのままなので、メールの再送は起きない
        assertThat(notificationService.sentEmails()).hasSize(2);
    }
}
