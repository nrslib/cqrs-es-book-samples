// 検証補助: 3.11
package org.example.cqrsesbooksamples.chapter03._43;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;


/**
 * プロパティによる初期セグメント数の設定（リスト3.11）の検証。
 * snippets/initial-segment-count.properties をそのまま読み込み、
 * プロパティ経路でも4セグメントで起動することを確かめる
 * （API経路は旧 3.19のInitialSegmentCountTestが検証している）。
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter03_43;DB_CLOSE_DELAY=-1")
@TestPropertySource(locations = "file:snippets/initial-segment-count.properties")
class InitialSegmentCountPropertiesTest {
    @Autowired
    private EventProcessingConfiguration config;

    @Test
    void プロパティの指定どおり4セグメントで起動する() {
        var processor = config.eventProcessor(
                "user-projection", PooledStreamingEventProcessor.class).orElseThrow();

        await().untilAsserted(() ->
                assertThat(processor.processingStatus()).hasSize(4));
    }
}
