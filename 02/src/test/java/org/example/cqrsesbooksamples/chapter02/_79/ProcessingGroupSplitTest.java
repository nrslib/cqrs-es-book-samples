// 検証補助: 2.61
package org.example.cqrsesbooksamples.chapter02._79;

import static org.assertj.core.api.Assertions.assertThat;

import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * 集約単位のProcessingGroup分割（リスト2.61）の検証。
 * みっつのプロジェクションがそれぞれ独立したグループのプロセッサに
 * 割り当てられることを確かめる。
 */
class ProcessingGroupSplitTest {

    private Configuration configuration;

    @AfterEach
    void tearDown() {
        if (configuration != null) {
            // configuration.shutdown() はJVM共有の AxonTaskJanitor まで停止させ、
            // 同一JVMで後続する他のテストのイベントプロセッサを壊すため、
            // プロセッサ単位で停止する
            configuration.eventProcessingConfiguration()
                    .eventProcessors()
                    .values()
                    .forEach(org.axonframework.eventhandling.EventProcessor::shutDown);
        }
    }

    @Test
    void 集約単位に分けたグループがそれぞれ独立したプロセッサになる() {
        var configurer = DefaultConfigurer.defaultConfiguration();
        configurer.eventProcessing()
                .usingSubscribingEventProcessors()
                .registerEventHandler(c -> new UserProjection())
                .registerEventHandler(c -> new OrderProjection())
                .registerEventHandler(c -> new InventoryProjection());
        configuration = configurer.start();

        var eventProcessing = configuration.eventProcessingConfiguration();
        var user = eventProcessing.eventProcessorByProcessingGroup("user-projection").orElseThrow();
        var order = eventProcessing.eventProcessorByProcessingGroup("order-projection").orElseThrow();
        var inventory = eventProcessing.eventProcessorByProcessingGroup("inventory-projection").orElseThrow();

        assertThat(user).isNotSameAs(order);
        assertThat(order).isNotSameAs(inventory);
    }
}
