// 紙面照合: 8.21, 8.22, 8.23
package org.example.cqrsesbooksamples.chapter08._21;

import static java.util.stream.Collectors.toList;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.deadletter.DeadLetter;
import org.axonframework.messaging.deadletter.SequencedDeadLetterQueue;
import org.axonframework.queryhandling.QueryGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter08_21;DB_CLOSE_DELAY=-1")
public class OrderProcessIntegrationTest {
    @Autowired
    private CommandGateway commandGateway;
    @Autowired
    private QueryGateway queryGateway;
    @Autowired
    private EventStore eventStore;
    @Autowired
    private EventProcessingConfiguration eventProcessingConfiguration;


    @Test
    void shouldCompleteOrderProcess() {
        var orderId = UUID.randomUUID().toString();
        var createOrder = new CreateOrderCommand(orderId, 1000);
        commandGateway.sendAndWait(createOrder);
        await()
            .atMost(Duration.ofSeconds(5))
            .until(() -> {
                var query = new GetOrderStatusQuery(orderId);
                var result = queryGateway.query(query, OrderStatus.class).join();
                return result == OrderStatus.COMPLETED;
            });
        var events = eventStore.readEvents(orderId).asStream();
        assertTrue(events.anyMatch(e -> e.getPayload() instanceof OrderCompletedEvent));
    }


    @Test
    void shouldHandleAsyncProcessing() {
        var orderId = UUID.randomUUID().toString();
        var createOrder = new CreateOrderCommand(orderId, 1000);

        commandGateway.sendAndWait(createOrder);

        await()
            .pollInterval(Duration.ofMillis(100))
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                var payloads = eventStore.readEvents(orderId).asStream()
                    .map(Message::getPayload)
                    .collect(toList());
                assertThat(payloads, hasItems(
                    instanceOf(OrderCreatedEvent.class),
                    instanceOf(PaymentProcessedEvent.class),
                    instanceOf(OrderCompletedEvent.class)
                ));
            });
    }


    @Test
    void shouldHandleFailedMessages() {
        var deadLetterQueue = eventProcessingConfiguration
            .deadLetterQueue("order-projection")
            .orElseThrow();
        var orderId = UUID.randomUUID().toString();
        commandGateway.sendAndWait(new CreateOrderCommand(orderId, -1000));

        await()
            .atMost(Duration.ofSeconds(5))
            .until(() -> findDeadLetter(deadLetterQueue, orderId).isPresent());

        var deadLetter = findDeadLetter(deadLetterQueue, orderId).orElseThrow();
        var cause = deadLetter.cause().orElseThrow();
        assertEquals("INVALID_AMOUNT", cause.message());
    }

    private Optional<DeadLetter<? extends EventMessage<?>>> findDeadLetter(
            SequencedDeadLetterQueue<EventMessage<?>> queue, String orderId) {
        return StreamSupport.stream(queue.deadLetters().spliterator(), false)
            .flatMap(sequence -> StreamSupport.stream(sequence.spliterator(), false))
            .filter(letter -> letter.message().getPayload() instanceof OrderCreatedEvent event
                && event.getOrderId().equals(orderId))
            .findFirst();
    }
}
