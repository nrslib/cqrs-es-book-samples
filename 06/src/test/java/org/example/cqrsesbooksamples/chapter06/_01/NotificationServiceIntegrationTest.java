package org.example.cqrsesbooksamples.chapter06._01;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.axonframework.eventhandling.EventHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter06_01_notification;DB_CLOSE_DELAY=-1")
class NotificationServiceIntegrationTest {

    @Autowired
    private PaymentsController controller;

    @Autowired
    private AccountQueryHandler accountQueryHandler;

    @Autowired
    private NotificationEventCollector collector;

    @TestConfiguration
    static class Config {
        @Bean
        NotificationEventCollector notificationEventCollector() {
            return new NotificationEventCollector();
        }
    }

    @Test
    void 支払い完了後にNotificationServiceが集約を再構築して通知イベントを発行する() {
        var accountId = UUID.randomUUID();
        accountQueryHandler.register(new AccountData(accountId, "Taro"));

        var response = controller.post(new PaymentsPostRequest(
            accountId, new BigDecimal("5000"), PaymentMethod.CREDIT));

        await().untilAsserted(() -> {
            assertThat(collector.events).anySatisfy(event -> {
                assertThat(event.paymentProcessId().value())
                    .isEqualTo(response.paymentProcessId());
                assertThat(event.accountId().value()).isEqualTo(accountId);
            });
        });
    }

    static class NotificationEventCollector {
        final List<PaymentCompletionNotifiedEvent> events = new CopyOnWriteArrayList<>();

        @EventHandler
        public void on(PaymentCompletionNotifiedEvent event) {
            events.add(event);
        }
    }
}
