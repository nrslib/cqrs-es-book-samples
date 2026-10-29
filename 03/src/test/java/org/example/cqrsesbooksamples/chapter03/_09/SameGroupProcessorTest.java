// 検証補助: 3.5
package org.example.cqrsesbooksamples.chapter03._09;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.GenericEventMessage;
import org.example.cqrsesbooksamples.chapter03._08.support.InventoryRepository;
import org.example.cqrsesbooksamples.chapter03._08.support.OrderPlacedEvent;
import org.example.cqrsesbooksamples.chapter03._08.support.OrderRepository;
import org.example.cqrsesbooksamples.chapter03._08.support.OrderSummary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * 同一ProcessingGroupへの集約（リスト3.5）の検証。
 * 単一ハンドラにまとめる形でも、複数クラスに同じグループ名を指定する形でも、
 * ひとつのプロセッサで各ハンドラが呼ばれることを確かめる。
 * リポジトリはモックのため、DBのコミットやロールバックはこのテストの対象外。
 */
class SameGroupProcessorTest {

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
    void 同じグループ名を指定したプロジェクションはひとつのプロセッサで処理される() {
        var orderRepository = mock(OrderRepository.class);
        var inventoryRepository = mock(InventoryRepository.class);

        var configurer = DefaultConfigurer.defaultConfiguration(false);
        configurer.eventProcessing()
                .usingSubscribingEventProcessors()
                .registerEventHandler(c -> new OrderProjection(orderRepository, inventoryRepository))
                .registerEventHandler(c -> new OrderSummaryProjection(orderRepository))
                .registerEventHandler(c -> new InventoryProjection(inventoryRepository));
        configuration = configurer.start();

        // みっつのクラスすべてが "order-projection" のプロセッサに集まる
        var eventProcessing = configuration.eventProcessingConfiguration();
        assertThat(eventProcessing.eventProcessorByProcessingGroup("order-projection")).isPresent();
        assertThat(eventProcessing.eventProcessors()).hasSize(1);

        // 発行したイベントは同じプロセッサ内ですべてのハンドラに処理される
        configuration.eventBus().publish(GenericEventMessage.asEventMessage(
                new OrderPlacedEvent("order-1", "product-1")));
        verify(orderRepository, times(2)).save(new OrderSummary("order-1"));
        verify(inventoryRepository, times(2)).decreaseStock("product-1");
    }
}
