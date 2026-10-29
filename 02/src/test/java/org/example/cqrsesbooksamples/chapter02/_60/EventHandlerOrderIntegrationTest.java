// 検証補助: 2.47
package org.example.cqrsesbooksamples.chapter02._60;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter02_60;DB_CLOSE_DELAY=-1",
        "axon.eventhandling.processors.user-management.mode=pooled",
        "axon.eventhandling.processors.user-management.initial-segment-count=1"
})
class EventHandlerOrderIntegrationTest {
    @Autowired
    private EventGateway eventGateway;

    @Autowired
    private NotificationService notificationService;

    // 削除済み（旧 2.78）
    @Test
    void 同じProcessingGroupではOrderの順にハンドラが実行される() {
        eventGateway.publish(new UserRegisteredEvent("user-123", "John", "john@example.com"));

        await().untilAsserted(() ->
            assertThat(notificationService.sentEmails()).contains("john@example.com"));

        // メール送信時点でリードモデルの更新が完了していた（@Order(1) が先に実行された）
        assertThat(notificationService.readModelExistedAtSend()).isTrue();
    }
}
