// 検証補助: 3.14
package org.example.cqrsesbooksamples.chapter03._47;

import static org.assertj.core.api.Assertions.assertThat;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * ワーカープールとクレーム上限の設定（リスト3.14）の検証。
 * maxClaimedSegments がプロセッサの受け入れ上限（maxCapacity）に
 * 反映されることを確かめる。
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter03_47;DB_CLOSE_DELAY=-1")
class MaxClaimedSegmentsConfigTest {
    @Autowired
    private EventProcessingConfiguration config;

    @Test
    void クレームできるセグメント数の上限が8になる() {
        var processor = config.eventProcessor(
                "user-projection", PooledStreamingEventProcessor.class).orElseThrow();

        assertThat(processor.maxCapacity()).isEqualTo(8);
    }
}
