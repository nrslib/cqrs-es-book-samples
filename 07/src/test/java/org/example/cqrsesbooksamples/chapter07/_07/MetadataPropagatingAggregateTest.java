package org.example.cqrsesbooksamples.chapter07._07;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.example.cqrsesbooksamples.chapter07.support.PaymentMethod;
import org.example.cqrsesbooksamples.chapter07.support.StartPaymentCommand;
import org.junit.jupiter.api.Test;

class MetadataPropagatingAggregateTest {
    @Test
    void コマンドのメタデータがイベントへ引き継がれる() {
        var fixture = new AggregateTestFixture<>(PaymentProcessAggregateAdapter.class);
        var processId = UUID.randomUUID().toString();

        fixture.givenNoPriorActivity()
            .when(new StartPaymentCommand(UUID.randomUUID(), new BigDecimal("5000"), PaymentMethod.CREDIT),
                  Map.of("processId", processId))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(org.axonframework.test.matchers.Matchers.exactSequenceOf(
                org.axonframework.test.matchers.Matchers.predicate(message -> {
                    assertThat(message.getMetaData().get("processId")).isEqualTo(processId);
                    return true;
                })));
    }
}
