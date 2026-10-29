// 検証補助: 2.63
package org.example.cqrsesbooksamples.chapter02._81;

import static org.assertj.core.api.Assertions.assertThat;

import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * 安定度によるProcessingGroup分割（リスト2.63）の検証。
 * 安定した中核と実験的な新機能が別グループの別プロセッサに割り当てられ、
 * 新機能側の失敗や停止が中核側に波及しない構成になることを確かめる。
 */
class StabilitySplitTest {

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
    void 中核と実験的機能が別プロセッサに分かれる() {
        var configurer = DefaultConfigurer.defaultConfiguration();
        configurer.eventProcessing()
                .usingSubscribingEventProcessors()
                .registerEventHandler(c -> new CoreBusinessProjection())
                .registerEventHandler(c -> new NewFeatureProjection());
        configuration = configurer.start();

        var eventProcessing = configuration.eventProcessingConfiguration();
        var stable = eventProcessing.eventProcessorByProcessingGroup("stable-projection").orElseThrow();
        var experimental = eventProcessing.eventProcessorByProcessingGroup("experimental-projection").orElseThrow();

        assertThat(stable).isNotSameAs(experimental);
    }
}
