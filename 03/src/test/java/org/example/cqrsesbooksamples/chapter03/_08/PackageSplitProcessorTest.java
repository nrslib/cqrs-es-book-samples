package org.example.cqrsesbooksamples.chapter03._08;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.GenericEventMessage;
import org.example.cqrsesbooksamples.chapter03._08.inventory.InventoryProjection;
import org.example.cqrsesbooksamples.chapter03._08.order.OrderProjection;
import org.example.cqrsesbooksamples.chapter03._08.support.InventoryRepository;
import org.example.cqrsesbooksamples.chapter03._08.support.OrderPlacedEvent;
import org.example.cqrsesbooksamples.chapter03._08.support.OrderRepository;
import org.example.cqrsesbooksamples.chapter03._08.support.OrderSummary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * パッケージが分かれたプロジェクション（旧 3.8）が
 * 別グループ・別トランザクション境界に割り当てられてしまうことの検証。
 * どちらも同じイベントを処理するが、プロセッサはパッケージごとに分かれる。
 */
class PackageSplitProcessorTest {

    private Configuration configuration;

    @AfterEach
    void tearDown() {
        if (configuration != null) {
            // configuration.shutdown() はJVM共有の AxonTaskJanitor まで停止させるため、
            // プロセッサ単位で停止する
            configuration.eventProcessingConfiguration()
                    .eventProcessors()
                    .values()
                    .forEach(EventProcessor::shutDown);
        }
    }

    @Test
    void パッケージごとに別のプロセッサに分かれてしまう() {
        var orderRepository = mock(OrderRepository.class);
        var inventoryRepository = mock(InventoryRepository.class);

        var configurer = DefaultConfigurer.defaultConfiguration(false);
        configurer.eventProcessing()
                .usingSubscribingEventProcessors()
                .registerEventHandler(c -> new OrderProjection(orderRepository))
                .registerEventHandler(c -> new InventoryProjection(inventoryRepository));
        configuration = configurer.start();

        // グループ名はそれぞれのパッケージ名になり、トランザクション境界が分かれる
        var eventProcessing = configuration.eventProcessingConfiguration();
        var orderProcessor = eventProcessing.eventProcessorByProcessingGroup(
                "org.example.cqrsesbooksamples.chapter03._08.order").orElseThrow();
        var inventoryProcessor = eventProcessing.eventProcessorByProcessingGroup(
                "org.example.cqrsesbooksamples.chapter03._08.inventory").orElseThrow();
        assertThat(orderProcessor).isNotSameAs(inventoryProcessor);

        // どちらのハンドラにもイベントは届く
        configuration.eventBus().publish(GenericEventMessage.asEventMessage(
                new OrderPlacedEvent("order-1", "product-1")));
        verify(orderRepository).save(new OrderSummary("order-1"));
        verify(inventoryRepository).decreaseStock("product-1");
    }
}
