// 検証補助: 2.49, 2.50, 2.51, 2.52, 2.53, 2.55
package org.example.cqrsesbooksamples.chapter02._63;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.eventhandling.tokenstore.UnableToClaimTokenException;
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore;
import org.axonframework.serialization.Serializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * トラッキングトークンとセグメントの転記リストを実際に動かして検証する。
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter02_63;DB_CLOSE_DELAY=-1",
        "axon.eventhandling.processors.user-projection.mode=pooled",
        "axon.eventhandling.processors.user-projection.initial-segment-count=2",
        "axon.eventhandling.processors.user-projection.thread-count=2"
})
class TokenEntryAndSegmentIntegrationTest {
    @Autowired
    private EventGateway eventGateway;

    @Autowired
    private EventProcessingConfiguration config;

    @Autowired
    private UserProjection projection;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManagerProvider entityManagerProvider;

    @Autowired
    private Serializer serializer;

    @Autowired
    private PlatformTransactionManager transactionManager;


    @Test
    void トークンはtoken_entryテーブルにセグメントごとに保存され処理位置を記録する() {
        var processor = config.eventProcessor("user-projection", PooledStreamingEventProcessor.class)
            .orElseThrow();

        // 2セグメントがそれぞれ独立したワーカーで起動する
        await().untilAsserted(() ->
            assertThat(processor.processingStatus()).hasSize(2));

        for (int i = 0; i < 10; i++) {
            eventGateway.publish(new UserRegisteredEvent("user-" + i, "User " + i, "user" + i + "@example.com"));
        }

        // 2セグメントが並列に処理しても、全イベントが取りこぼしなく処理される
        await().untilAsserted(() ->
            assertThat(projection.handledUserIds()).hasSize(10));

        // token_entryテーブルにはProcessingGroupごと・セグメントごとにトークンが保存される
        var rows = jdbcTemplate.queryForList(
            "SELECT processor_name, segment, owner FROM token_entry "
                + "WHERE processor_name = 'user-projection' ORDER BY segment");
        assertThat(rows).hasSize(2);
        assertThat(((Number) rows.get(0).get("segment")).intValue()).isEqualTo(0);
        assertThat(((Number) rows.get(1).get("segment")).intValue()).isEqualTo(1);

        // トークンは「どこまで処理したか」の位置を保持する
        await().untilAsserted(() ->
            processor.processingStatus().values().forEach(status ->
                assertThat(status.getCurrentPosition()).isPresent()));

        // トークンの実体はGapAwareTrackingToken（indexが処理済み位置、gapsが未確認の追い越し位置）
        await().untilAsserted(() -> {
            var tokenBytes = jdbcTemplate.queryForObject(
                "SELECT token FROM token_entry "
                    + "WHERE processor_name = 'user-projection' AND segment = 0",
                byte[].class);
            assertThat(tokenBytes).isNotNull();
            assertThat(new String(tokenBytes, java.nio.charset.StandardCharsets.UTF_8))
                .contains("\"index\"")
                .contains("\"gaps\"");
        });
    }

    // 削除済み（旧 2.70）
    @Test
    void セグメントは複数インスタンスへ分散しクレーム解放後に別インスタンスへ渡る() {
        String processorName = "distributed-projection";
        var instance1 = tokenStore("instance-1");
        var instance2 = tokenStore("instance-2");
        var instance3 = tokenStore("instance-3");
        var template = new TransactionTemplate(transactionManager);

        template.executeWithoutResult(tx -> instance1.initializeTokenSegments(processorName, 4));
        template.executeWithoutResult(tx -> instance1.fetchToken(processorName, 0));
        template.executeWithoutResult(tx -> instance1.fetchToken(processorName, 1));
        template.executeWithoutResult(tx -> instance2.fetchToken(processorName, 2));
        template.executeWithoutResult(tx -> instance2.fetchToken(processorName, 3));

        var distributed = jdbcTemplate.queryForList(
            "SELECT segment, owner FROM token_entry WHERE processor_name = ? ORDER BY segment",
            processorName);
        assertThat(distributed)
            .extracting(row -> row.get("owner"))
            .containsExactly("instance-1", "instance-1", "instance-2", "instance-2");

        template.executeWithoutResult(tx -> instance2.releaseClaim(processorName, 3));
        template.executeWithoutResult(tx -> instance3.fetchToken(processorName, 3));

        var redistributed = jdbcTemplate.queryForList(
            "SELECT segment, owner FROM token_entry WHERE processor_name = ? ORDER BY segment",
            processorName);
        assertThat(redistributed)
            .extracting(row -> row.get("owner"))
            .containsExactly("instance-1", "instance-1", "instance-2", "instance-3");
    }

    @Test
    void 他のインスタンスがクレーム済みのセグメントはクレームできない() {
        var processor = config.eventProcessor("user-projection", PooledStreamingEventProcessor.class)
            .orElseThrow();
        await().untilAsserted(() ->
            assertThat(processor.processingStatus()).hasSize(2));

        var anotherInstanceStore = tokenStore("claim-conflict-instance");
        var template = new TransactionTemplate(transactionManager);
        assertThatThrownBy(() -> template.executeWithoutResult(tx ->
            anotherInstanceStore.fetchToken("user-projection", 0)))
            .isInstanceOf(UnableToClaimTokenException.class);
    }

    private JpaTokenStore tokenStore(String nodeId) {
        return JpaTokenStore.builder()
            .entityManagerProvider(entityManagerProvider)
            .serializer(serializer)
            .nodeId(nodeId)
            .build();
    }
}
