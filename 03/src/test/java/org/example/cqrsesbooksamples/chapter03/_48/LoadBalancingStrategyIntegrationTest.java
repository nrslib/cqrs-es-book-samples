// 検証補助: 3.15
package org.example.cqrsesbooksamples.chapter03._48;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

import org.axonframework.axonserver.connector.AxonServerConfiguration;
import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


/**
 * セグメントのロードバランシング戦略の設定（リスト3.15）の検証。
 * snippets/load-balancing.properties をそのまま読み込み、Docker上の実Axon Serverに
 * 接続した状態で、戦略がAxon Server連携の構成に束縛され、イベントが実サーバ経由で
 * プロセッサに届くことを確かめる。Dockerがない環境では自動的にスキップされる。
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter03_48;DB_CLOSE_DELAY=-1",
        "axon.eventhandling.processors.user-projection.mode=pooled",
        "axon.eventhandling.processors.user-projection.initial-segment-count=1"
})
@TestPropertySource(locations = "file:snippets/load-balancing.properties")
class LoadBalancingStrategyIntegrationTest {

    @Container
    static final GenericContainer<?> axonServer =
            new GenericContainer<>("axoniq/axonserver:latest")
                    .withEnv("AXONIQ_AXONSERVER_STANDALONE", "TRUE")
                    .withExposedPorts(8024, 8124)
                    // ヘルスチェックはgRPC(8124)の受け入れ準備より先にUPになることがあるため、
                    // 起動完了ログまで待つ
                    .waitingFor(Wait.forLogMessage(".*Started AxonServer.*", 1)
                            .withStartupTimeout(Duration.ofMinutes(5)));

    @DynamicPropertySource
    static void axonServerProperties(DynamicPropertyRegistry registry) {
        // 起動完了ログの後もdefaultコンテキストの初期化が終わるまでgRPCがUNAVAILABLEを
        // 返すことがある。アプリ側の接続リトライ(30回)を使い切ってしまわないよう、
        // 同じgRPC経路で接続可能になるまで待ってからコンテキストを起動する
        waitUntilGrpcReady();
        registry.add("axon.axonserver.enabled", () -> "true");
        registry.add("axon.axonserver.servers",
                () -> axonServer.getHost() + ":" + axonServer.getMappedPort(8124));
    }

    private static void waitUntilGrpcReady() {
        var factory = io.axoniq.axonserver.connector.AxonServerConnectionFactory
                .forClient("readiness-probe")
                .routingServers(new io.axoniq.axonserver.connector.impl.ServerAddress(
                        axonServer.getHost(), axonServer.getMappedPort(8124)))
                .build();
        try {
            var connection = factory.connect("default");
            await().atMost(Duration.ofMinutes(2))
                    .pollInterval(Duration.ofMillis(500))
                    .until(connection::isReady);
        } finally {
            factory.shutdown();
        }
    }

    @Autowired
    private AxonServerConfiguration axonServerConfiguration;

    @Autowired
    private AxonServerConnectionManager connectionManager;

    @Autowired
    private EventGateway eventGateway;

    @Test
    void 実AxonServerへの接続と戦略設定が有効になる() {
        // snippetのプロパティがAxon Server連携の構成に束縛されている
        var processor = axonServerConfiguration.getEventhandling()
                .getProcessors().get("user-projection");
        assertThat(processor.getLoadBalancingStrategy()).isEqualTo("threadNumber");
        assertThat(processor.isAutomaticBalancing()).isTrue();

        // 実サーバとの接続が確立している
        await().atMost(Duration.ofSeconds(60)).untilAsserted(() ->
                assertThat(connectionManager.getConnection().isConnected()).isTrue());

        // 実サーバのイベントストアを経由してプロセッサにイベントが届く
        eventGateway.publish(new Chapter03_48TestApplication.UserRegisteredEvent("user-1"));
        await().atMost(Duration.ofSeconds(60)).untilAsserted(() ->
                assertThat(Chapter03_48TestApplication.UserProjectionStub.received)
                        .contains("user-1"));
    }
}
