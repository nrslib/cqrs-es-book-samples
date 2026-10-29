// 検証補助: 3.43
package org.example.cqrsesbooksamples.chapter03._59;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * エラーを握りつぶさず伝搬させる設定（リスト3.42）の検証。
 * PropagatingErrorHandler により、失敗したイベントが再試行され続け、
 * トークンが先へ進まない（=イベントを取りこぼさない）ことを確かめる。
 * 既定の LoggingErrorHandler ならログを出して次へ進むため、再試行は起きない。
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter03_59;DB_CLOSE_DELAY=-1",
        "axon.eventhandling.processors.user-projection.mode=pooled",
        "axon.eventhandling.processors.user-projection.initial-segment-count=1"
})
class PropagatingErrorHandlerConfigTest {
    @Autowired
    private EventGateway eventGateway;

    @Autowired
    private EventProcessingConfiguration config;

    @Test
    void ハンドラの失敗が伝搬して同じイベントが再試行され続ける() {
        eventGateway.publish(new Chapter03_59TestApplication.PoisonEvent("poison-1"));

        var processor = config.eventProcessor(
                "user-projection", PooledStreamingEventProcessor.class).orElseThrow();

        // エラーが伝搬してバッチが巻き戻り、同じイベントの処理が繰り返される
        await().untilAsserted(() ->
                assertThat(Chapter03_59TestApplication.FailingProjectionStub.attempts.get())
                        .isGreaterThanOrEqualTo(2));

        // 失敗したイベントを飛ばさないため、トークンは前進しない
        assertThat(processor.processingStatus().values())
                .allMatch(status -> status.getCurrentPosition().orElse(-1L) < 0);
    }
}
