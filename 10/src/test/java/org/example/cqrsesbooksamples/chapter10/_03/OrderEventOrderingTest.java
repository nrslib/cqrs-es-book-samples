package org.example.cqrsesbooksamples.chapter10._03;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;

import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * 同一集約のイベント順序（旧 10.3）の検証。
 * 同じ集約識別子のイベントが、シーケンス番号順にプロジェクションへ届くことを確かめる。
 */
class OrderEventOrderingTest {

    private Configuration configuration;

    @AfterEach
    void tearDown() {
        if (configuration != null) {
            configuration.eventProcessingConfiguration()
                    .eventProcessors()
                    .values()
                    .forEach(EventProcessor::shutDown);
        }
    }

    @Test
    void 同一集約のイベントを発生順に処理する() {
        var projection = spy(new OrderProjection());
        var configurer = DefaultConfigurer.defaultConfiguration(false);
        configurer.eventProcessing()
                .usingSubscribingEventProcessors()
                .registerEventHandler(c -> projection);
        configuration = configurer.start();

        var orderId = new OrderId("order-1");
        var created = new OrderCreatedEvent(orderId);
        var updated = new OrderUpdatedEvent(orderId);

        configuration.eventBus().publish(
                new GenericDomainEventMessage<>("Order", orderId.toString(), 0, created),
                new GenericDomainEventMessage<>("Order", orderId.toString(), 1, updated));

        var processingOrder = inOrder(projection);
        processingOrder.verify(projection).on(created);
        processingOrder.verify(projection).on(updated);
    }
}
