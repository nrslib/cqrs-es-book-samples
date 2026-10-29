// 検証補助: 4.14, 4.15, 4.16
package org.example.cqrsesbooksamples.chapter04._15;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


/**
 * 従来的なテーブル構造と参照クエリ（リスト4.14〜4.16）の検証。
 * snippets/traditional-tables.sql と snippets/join-aggregation-query.sql を
 * そのまま実PostgreSQLで実行し、結合・集計クエリとリードモデルへの
 * 単純な検索がどちらも同じ結果を返すことを確かめる。
 * Dockerがない環境では自動的にスキップされる。
 */
@Testcontainers(disabledWithoutDocker = true)
class TraditionalTablesQueryTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Test
    void 結合集計クエリとリードモデル検索が同じ件数を返す() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement statement = connection.createStatement()) {

            // のテーブル定義をそのまま実行する
            for (String sql : readStatements("snippets/traditional-tables.sql")) {
                statement.execute(sql);
            }

            // 文書2件と「役に立った」3件を用意する
            statement.execute("insert into documents values ('doc-1', 'CQRS入門', '本文1')");
            statement.execute("insert into documents values ('doc-2', 'ES入門', '本文2')");
            statement.execute("insert into helpful_marks values ('doc-1', 'user-1')");
            statement.execute("insert into helpful_marks values ('doc-1', 'user-2')");
            statement.execute("insert into helpful_marks values ('doc-2', 'user-1')");

            var statements = readStatements("snippets/join-aggregation-query.sql");

            // 参照のたびに結合と集計が走るクエリ
            Map<String, Long> aggregated = new LinkedHashMap<>();
            try (var resultSet = statement.executeQuery(statements.get(0))) {
                while (resultSet.next()) {
                    aggregated.put(resultSet.getString("document_id"),
                            resultSet.getLong("helpful_count"));
                }
            }
            assertThat(aggregated)
                    .containsEntry("doc-1", 2L)
                    .containsEntry("doc-2", 1L);

            // 事前集計済みリードモデルへの単純な検索
            // （プロジェクションが構築するリードモデルを用意して同じ結果になることを見る）
            statement.execute("""
                    create table document_views as
                    select d.document_id, d.title, count(h.user_id) as helpful_count
                    from documents d
                    left join helpful_marks h on d.document_id = h.document_id
                    group by d.document_id, d.title""");
            Map<String, Long> readModel = new LinkedHashMap<>();
            try (var resultSet = statement.executeQuery(statements.get(1))) {
                while (resultSet.next()) {
                    readModel.put(resultSet.getString("document_id"),
                            resultSet.getLong("helpful_count"));
                }
            }
            assertThat(readModel).isEqualTo(aggregated);
        }
    }

    private List<String> readStatements(String path) throws Exception {
        String script = Files.readString(Path.of(path));
        List<String> statements = new ArrayList<>();
        for (String sql : script.split(";")) {
            if (!sql.isBlank()) {
                statements.add(sql);
            }
        }
        return statements;
    }
}
