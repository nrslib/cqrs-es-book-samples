package org.example.cqrsesbooksamples.chapter03._42;

import static org.assertj.core.api.Assertions.assertThat;

import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventhandling.EventProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * ＠ProcessingGroupによるグループ名の固定（旧 3.17）の検証。
 * クラスやパッケージをリファクタリングしてもグループ名（＝トークンの帰属先）が
 * アノテーションの値に固定されることを確かめる。
 */
class ProcessingGroupRenameTest {

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
    void グループ名はパッケージ名ではなくアノテーションの値になる() {
        var configurer = DefaultConfigurer.defaultConfiguration(false);
        configurer.eventProcessing()
                .usingSubscribingEventProcessors()
                .registerEventHandler(c -> new UserProjection());
        configuration = configurer.start();

        var eventProcessing = configuration.eventProcessingConfiguration();
        assertThat(eventProcessing.eventProcessorByProcessingGroup("user")).isPresent();
        assertThat(eventProcessing.eventProcessorByProcessingGroup(
                "org.example.cqrsesbooksamples.chapter03._42")).isEmpty();
    }
}
