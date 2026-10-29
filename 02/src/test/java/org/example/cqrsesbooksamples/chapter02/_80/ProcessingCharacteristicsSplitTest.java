// 検証補助: 2.62
package org.example.cqrsesbooksamples.chapter02._80;

import static org.assertj.core.api.Assertions.assertThat;

import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * 処理特性によるProcessingGroup分割（リスト2.62）の検証。
 * バッチ系とリアルタイム系が別グループの別プロセッサに割り当てられ、
 * 特性ごとに独立して制御できる構成になることを確かめる。
 */
class ProcessingCharacteristicsSplitTest {

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
    void バッチ系とリアルタイム系が別プロセッサに分かれる() {
        var configurer = DefaultConfigurer.defaultConfiguration();
        configurer.eventProcessing()
                .usingSubscribingEventProcessors()
                .registerEventHandler(c -> new DailySalesReportProjection())
                .registerEventHandler(c -> new RealtimeOrderStatusProjection());
        configuration = configurer.start();

        var eventProcessing = configuration.eventProcessingConfiguration();
        var batch = eventProcessing.eventProcessorByProcessingGroup("batch-projection").orElseThrow();
        var realtime = eventProcessing.eventProcessorByProcessingGroup("realtime-projection").orElseThrow();

        assertThat(batch).isNotSameAs(realtime);
    }
}
