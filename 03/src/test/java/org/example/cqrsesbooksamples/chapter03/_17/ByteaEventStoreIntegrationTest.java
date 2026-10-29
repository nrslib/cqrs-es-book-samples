// 検証補助: 3.22, 3.23, 3.24
package org.example.cqrsesbooksamples.chapter03._17;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


/**
 * BYTEA型で保存するPostgreSQL構成（リスト3.22〜3.24）の検証。
 * カスタムダイアレクト（リスト3.22）とorm.xml（リスト3.24、snippets/orm.xml を
 * そのまま読み込み）をリスト3.23の spring.jpa.database-platform 経由で適用し、
 * 実PostgreSQL上で payload・token 列が OID ではなく BYTEA になり、
 * その構成でイベントの保存と処理が通ることを確かめる。
 * Dockerがない環境では自動的にスキップされる。
 * （リスト3.23の snippet はダイアレクトのFQCNを書籍上の配置で示しているため、
 * ここでは同じプロパティキーに本リポジトリ上のFQCNを指定している）
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.jpa.database-platform=org.example.cqrsesbooksamples.chapter03._17.ByteaPostgreSQLDialect",
        "spring.jpa.mapping-resources=orm.xml",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        // 03モジュール既定のH2用schema.sql（階層型ストレージの例示用）はPostgreSQLでは流さない
        "spring.sql.init.mode=never",
        "axon.eventhandling.processors.user-projection.mode=pooled",
        "axon.eventhandling.processors.user-projection.initial-segment-count=1"
})
class ByteaEventStoreIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private EventGateway eventGateway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void イベントとトークンがBYTEA型の列に保存される() {
        // ダイアレクトとorm.xmlの適用で、既定のOIDではなくBYTEAでスキーマが生成される
        assertThat(columnType("domain_event_entry", "payload")).isEqualTo("bytea");
        assertThat(columnType("domain_event_entry", "meta_data")).isEqualTo("bytea");
        assertThat(columnType("snapshot_event_entry", "payload")).isEqualTo("bytea");
        assertThat(columnType("token_entry", "token")).isEqualTo("bytea");

        // BYTEA構成のままイベントの保存と処理が通る
        eventGateway.publish(new Chapter03_17TestApplication.UserRegisteredEvent("user-1"));
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(Chapter03_17TestApplication.UserProjectionStub.received).contains("user-1");
            assertThat(jdbcTemplate.queryForObject(
                    "select count(*) from domain_event_entry", Long.class)).isEqualTo(1L);
        });
    }

    private String columnType(String table, String column) {
        return jdbcTemplate.queryForObject(
                "select data_type from information_schema.columns"
                        + " where table_name = ? and column_name = ?",
                String.class, table, column);
    }
}
