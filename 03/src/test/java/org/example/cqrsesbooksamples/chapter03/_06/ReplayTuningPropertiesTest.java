package org.example.cqrsesbooksamples.chapter03._06;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.axonframework.springboot.EventProcessorProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// 削除済み（旧 3.6）
/**
 * リプレイ速度を調整するプロセッサ設定（旧 3.6）の検証。
 * snippets/replay-tuning.properties をそのまま読み込み、
 * バッチサイズ500・8セグメントのPooledプロセッサとして起動することを確かめる。
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter03_06;DB_CLOSE_DELAY=-1")
@TestPropertySource(locations = "file:snippets/replay-tuning.properties")
class ReplayTuningPropertiesTest {
    @Autowired
    private EventProcessingConfiguration config;

    @Autowired
    private EventProcessorProperties processorProperties;

    @Test
    void バッチサイズ500の8セグメント構成で起動する() {
        // プロパティがバッチサイズ500として束縛されている
        var settings = processorProperties.getProcessors().get("user-projection-v2");
        assertThat(settings.getBatchSize()).isEqualTo(500);

        // 8セグメントのPooledプロセッサとして起動する
        var processor = config.eventProcessor(
                "user-projection-v2", PooledStreamingEventProcessor.class).orElseThrow();
        await().untilAsserted(() ->
                assertThat(processor.processingStatus()).hasSize(8));
    }
}
