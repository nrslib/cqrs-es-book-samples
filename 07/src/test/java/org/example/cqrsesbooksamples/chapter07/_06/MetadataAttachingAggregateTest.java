package org.example.cqrsesbooksamples.chapter07._06;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessAcceptedEvent;
import org.example.cqrsesbooksamples.chapter07.support.PaymentMethod;
import org.example.cqrsesbooksamples.chapter07.support.StartPaymentCommand;
import org.junit.jupiter.api.Test;

class MetadataAttachingAggregateTest {
    @Test
    void イベント発行時にプロセスIDがメタデータとして付与される() {
        var fixture = new AggregateTestFixture<>(PaymentProcessAggregateAdapter.class);
        fixture.givenNoPriorActivity()
            .when(new StartPaymentCommand(UUID.randomUUID(), new BigDecimal("5000"), PaymentMethod.CREDIT))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(org.axonframework.test.matchers.Matchers.exactSequenceOf(
                org.axonframework.test.matchers.Matchers.predicate(message -> {
                    var processId = message.getMetaData().get("processId");
                    var payload = (PaymentProcessAcceptedEvent) message.getPayload();
                    assertThat(processId).isEqualTo(payload.paymentProcessId().value());
                    return true;
                })));
    }

    @Test
    void 呼び出し側が採番したプロセスIDが集約のイベントへ引き継がれる() {
        var fixture = new AggregateTestFixture<>(PaymentProcessAggregateAdapter.class);
        var processId = UUID.randomUUID();

        fixture.givenNoPriorActivity()
            .when(new StartPaymentCommand(processId, UUID.randomUUID(), new BigDecimal("5000"), PaymentMethod.CREDIT))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(org.axonframework.test.matchers.Matchers.exactSequenceOf(
                org.axonframework.test.matchers.Matchers.predicate(message -> {
                    var payload = (PaymentProcessAcceptedEvent) message.getPayload();
                    // 採番した processId がそのまま集約IDとメタデータに載り、応答IDと一致する
                    assertThat(payload.paymentProcessId().value()).isEqualTo(processId.toString());
                    assertThat(message.getMetaData().get("processId")).isEqualTo(processId.toString());
                    return true;
                })));
    }
}
