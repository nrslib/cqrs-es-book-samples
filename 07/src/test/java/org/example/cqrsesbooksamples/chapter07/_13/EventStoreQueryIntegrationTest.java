package org.example.cqrsesbooksamples.chapter07._13;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessAcceptedEvent;
import org.example.cqrsesbooksamples.chapter07.support.PaymentMethod;
import org.example.cqrsesbooksamples.chapter07.support.PaymentProcessId;
import org.example.cqrsesbooksamples.chapter07.support.StartPaymentCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = Chapter07_13TestApplication.class,
        properties = "spring.datasource.url=jdbc:h2:mem:chapter07_13;DB_CLOSE_DELAY=-1")
class EventStoreQueryIntegrationTest {
    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private PaymentProcessEventStoreQuery query;

    @Test
    void 集約単位の読み出しとメタデータ探索の両方で追跡できる() {
        PaymentProcessId processId = commandGateway.sendAndWait(
            new StartPaymentCommand(UUID.randomUUID(), new BigDecimal("5000"), PaymentMethod.CREDIT));

        // 集約IDを指定した直接読み出し
        var aggregateEvents = query.getEventsForAggregate(processId.value());
        assertThat(aggregateEvents).hasSize(1);
        assertThat(aggregateEvents.get(0).getPayload()).isInstanceOf(PaymentProcessAcceptedEvent.class);

        // メタデータに processId を含むイベントの探索
        var byMetadata = query.getEventsWithMetadata(processId.value());
        assertThat(byMetadata).hasSize(1);

        // 存在しない processId では何も見つからない
        assertThat(query.getEventsWithMetadata("unknown")).isEmpty();
    }
}
