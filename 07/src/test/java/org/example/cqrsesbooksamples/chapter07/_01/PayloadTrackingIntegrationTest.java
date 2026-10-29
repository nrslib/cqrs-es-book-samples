package org.example.cqrsesbooksamples.chapter07._01;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.example.cqrsesbooksamples.chapter07.support.AccountId;
import org.example.cqrsesbooksamples.chapter07.support.NotFoundException;
import org.example.cqrsesbooksamples.chapter07.support.PaymentMethod;
import org.example.cqrsesbooksamples.chapter07.support.PaymentProcessId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter07_01;DB_CLOSE_DELAY=-1",
        // 既定16セグメントだとスイート同時実行時にこの集約のセグメント処理が遅延するため1セグメントに固定する
        "axon.eventhandling.processors[org.example.cqrsesbooksamples.chapter07._01].initial-segment-count=1"
})
class PayloadTrackingIntegrationTest {
    @Autowired
    private EventGateway eventGateway;

    @Autowired
    private PaymentProcessStatusController controller;

    @Test
    void ペイロードのプロセスIDで進行状況を追跡できる() {
        var processId = new PaymentProcessId();
        var accountId = new AccountId(UUID.randomUUID());

        eventGateway.publish(new PaymentProcessAcceptedEvent(
            processId, accountId, new BigDecimal("5000"), PaymentMethod.CREDIT));

        // PSEPは非同期に追従するため、未反映時のNotFoundExceptionは無視して再試行する
        await().atMost(Duration.ofSeconds(30)).ignoreExceptions().untilAsserted(() -> {
            var response = controller.getStatus(processId.value());
            assertThat(response.currentState()).isEqualTo(PaymentProcessState.STARTED);
        });

        eventGateway.publish(new CreditAcceptedEvent(processId));
        eventGateway.publish(new PaymentProcessCompletedEvent(processId));

        await().atMost(Duration.ofSeconds(30)).ignoreExceptions().untilAsserted(() -> {
            var response = controller.getStatus(processId.value());
            assertThat(response.currentState()).isEqualTo(PaymentProcessState.COMPLETED);
        });
    }

    @Test
    void 存在しないプロセスIDは404になる() {
        assertThatThrownBy(() -> controller.getStatus("unknown-process"))
            .isInstanceOf(NotFoundException.class);
    }
}
