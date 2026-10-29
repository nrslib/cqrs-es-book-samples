// 検証補助: 3.25
package org.example.cqrsesbooksamples.chapter03._20;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


/**
 * OID型からBYTEA型へ移行するSQL（リスト3.25）の検証。
 * 実PostgreSQL上でOID（Large Object）のtoken列を持つテーブルを用意し、
 * snippets/oid-to-bytea.sql をそのまま実行して、データを保ったまま
 * BYTEA列へ移行され、Large Object本体も削除されることを確かめる。
 * Dockerがない環境では自動的にスキップされる。
 */
@Testcontainers(disabledWithoutDocker = true)
class OidToByteaMigrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Test
    void OIDのトークンをデータを保ったままBYTEAへ移行できる() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement statement = connection.createStatement()) {

            // 移行前の状態: token列はOID型で、Large Objectとしてデータを保持している
            statement.execute("""
                    create table token_entry (
                        processor_name varchar(255) not null,
                        segment        integer not null,
                        token          oid,
                        primary key (processor_name, segment)
                    )""");
            statement.execute("insert into token_entry values"
                    + " ('user-projection', 0, lo_from_bytea(0, 'token-data'::bytea))");

            // snippetの移行SQLをそのまま実行する
            String migration = Files.readString(Path.of("snippets/oid-to-bytea.sql"));
            for (String sql : migration.split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }

            // 列はBYTEA型になり、データは保たれている
            try (var resultSet = statement.executeQuery(
                    "select data_type from information_schema.columns"
                            + " where table_name = 'token_entry' and column_name = 'token'")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString(1)).isEqualTo("bytea");
            }
            try (var resultSet = statement.executeQuery("select token from token_entry")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(Arrays.equals(resultSet.getBytes(1), "token-data".getBytes())).isTrue();
            }

            // lo_unlink により Large Object 本体も残っていない
            try (var resultSet = statement.executeQuery(
                    "select count(*) from pg_largeobject_metadata")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getLong(1)).isZero();
            }
        }
    }
}
