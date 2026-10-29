package org.example.cqrsesbooksamples.chapter06._01;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter06_01;DB_CLOSE_DELAY=-1")
class PaymentFlowIntegrationTest {
    @Autowired
    private PaymentsController controller;

    @Autowired
    private AccountQueryHandler accountQueryHandler;

    @Autowired
    private EventStore eventStore;

    @Autowired
    private EventGateway eventGateway;

    @Test
    void クレジット決済は外部API承認から通知を経て完了まで連鎖する() {
        var accountId = UUID.randomUUID();
        accountQueryHandler.register(new AccountData(accountId, "Taro"));

        var response = controller.post(new PaymentsPostRequest(
            accountId, new BigDecimal("5000"), PaymentMethod.CREDIT));

        await().untilAsserted(() -> {
            List<Object> payloads = eventStore.readEvents(response.paymentProcessId()).asStream()
                .map(message -> (Object) message.getPayload())
                .toList();
            assertThat(payloads).anySatisfy(payload ->
                assertThat(payload).isInstanceOf(PaymentProcessCompletedEvent.class));
        });
    }

    @Test
    void 銀行振込は依頼登録の後で入金検知が合流して完了する() {
        var accountId = UUID.randomUUID();
        accountQueryHandler.register(new AccountData(accountId, "Jiro"));

        var response = controller.post(new PaymentsPostRequest(
            accountId, new BigDecimal("10000"), PaymentMethod.BANK_TRANSFER));
        var processId = new PaymentProcessId(response.paymentProcessId());

        // 依頼の登録までは自動で進む（この時点では完了しない）
        await().untilAsserted(() -> {
            List<Object> stored = eventStore.readEvents(processId.value()).asStream()
                .map(message -> (Object) message.getPayload())
                .toList();
            assertThat(stored).noneSatisfy(payload ->
                assertThat(payload).isInstanceOf(PaymentProcessCompletedEvent.class));
        });

        // 入金の検知を模してイベントを流すと、Aポリシーが合流して完了まで進む
        eventGateway.publish(new BankTransferDetectedEvent(processId));

        await().untilAsserted(() -> {
            List<Object> payloads = eventStore.readEvents(processId.value()).asStream()
                .map(message -> (Object) message.getPayload())
                .toList();
            assertThat(payloads).anySatisfy(payload ->
                assertThat(payload).isInstanceOf(PaymentProcessCompletedEvent.class));
        });
    }

    @Test
    void 存在しないアカウントへの支払いは404になる() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
            controller.post(new PaymentsPostRequest(
                UUID.randomUUID(), new BigDecimal("5000"), PaymentMethod.CREDIT)))
            .isInstanceOf(NotFoundException.class);
    }
}
