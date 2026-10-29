package org.example.cqrsesbooksamples.chapter02._62;

import static org.assertj.core.api.Assertions.assertThat;

import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * ProcessingGroup名の決まり方（旧 2.62）の検証。
 * 指定なしはパッケージ名、指定ありはアノテーションの値がグループ名になり、
 * 同じ名前を指定したクラスは同じプロセッサに集まることを確かめる。
 */
class ProcessingGroupNamingTest {

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
    void 指定なしはパッケージ名が指定ありはアノテーション値がグループ名になる() {
        var configurer = DefaultConfigurer.defaultConfiguration();
        configurer.eventProcessing()
                .usingSubscribingEventProcessors()
                .registerEventHandler(c -> new org.example.cqrsesbooksamples.chapter02._62.defaults.UserProjection())
                .registerEventHandler(c -> new org.example.cqrsesbooksamples.chapter02._62.named.UserProjection())
                .registerEventHandler(c -> new org.example.cqrsesbooksamples.chapter02._62.named.UserSummaryProjection());
        configuration = configurer.start();

        var eventProcessing = configuration.eventProcessingConfiguration();

        // 指定なし: パッケージ名がそのままグループ名になる
        assertThat(eventProcessing.eventProcessorByProcessingGroup(
                "org.example.cqrsesbooksamples.chapter02._62.defaults")).isPresent();

        // 指定あり: "user-projection" グループになり、同名指定の2クラスは同じプロセッサに集まる
        var named = eventProcessing.eventProcessorByProcessingGroup("user-projection").orElseThrow();
        assertThat(named.getName()).isEqualTo("user-projection");
    }
}
