// 紙面照合: 2.6
// 検証補助: 2.18, 2.19, 2.20, 2.21, 2.22, 2.24, 2.32, 2.33
// 検証補助: 2.42, 2.43
package org.example.cqrsesbooksamples.chapter02._2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter02_02;DB_CLOSE_DELAY=-1")
class UserCrudIntegrationTest {
    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private QueryGateway queryGateway;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    void 作成から削除までのCRUDフローがリードモデルへ反映される() {
        // コンストラクタのコマンドハンドラは集約IDを返す
        var command = new CreateUserCommand("John", "john@example.com");
        // 同期
        String userId = commandGateway.sendAndWait(command);
        assertThat(userId).isNotNull();

        // 非同期送信も同じ集約IDを返す
        command = new CreateUserCommand("Jane", "jane@example.com");
        // 非同期
        CompletableFuture<String> result = commandGateway.send(command);
        assertThat(result.join()).isNotNull();

        // Read: プロジェクションが非同期でリードモデルを構築する
        await().untilAsserted(() -> {
            var queryResult = queryGateway.query(new FindUserQuery(userId), FindUserQueryResult.class).join();
            assertThat(queryResult.user()).isPresent();
            assertThat(queryResult.user().orElseThrow().getName()).isEqualTo("John");
        });

        // リードモデルのテーブルにはユーザーが1行で保存されている
        var userRow = jdbcTemplate.queryForMap("SELECT * FROM users WHERE user_id = ?", userId);
        assertThat(userRow.get("name")).isEqualTo("John");
        assertThat(userRow.get("email")).isEqualTo("john@example.com");

        // Update
        commandGateway.sendAndWait(new UpdateUserCommand(userId, "John Doe", "john.doe@example.com"));
        await().untilAsserted(() -> {
            var queryResult = queryGateway.query(new FindUserQuery(userId), FindUserQueryResult.class).join();
            assertThat(queryResult.user().orElseThrow().getName()).isEqualTo("John Doe");
            assertThat(queryResult.user().orElseThrow().getEmail()).isEqualTo("john.doe@example.com");
        });

        // イベントストアには作成と更新がシーケンス番号順に記録されている
        assertThat(eventTypes(userId)).containsExactly("UserCreatedEvent", "UserUpdatedEvent");

        // Delete
        commandGateway.sendAndWait(new DeleteUserCommand(userId));
        await().untilAsserted(() -> {
            var queryResult = queryGateway.query(new FindUserQuery(userId), FindUserQueryResult.class).join();
            assertThat(queryResult.user()).isEmpty();
        });

        // 削除も既存イベントを消さず、削除イベントとして追記される
        assertThat(eventTypes(userId))
            .containsExactly("UserCreatedEvent", "UserUpdatedEvent", "UserDeletedEvent");
    }

    private List<String> eventTypes(String aggregateIdentifier) {
        return jdbcTemplate.queryForList(
                "SELECT payload_type FROM domain_event_entry "
                    + "WHERE aggregate_identifier = ? ORDER BY sequence_number",
                String.class, aggregateIdentifier)
            .stream()
            .map(type -> type.substring(type.lastIndexOf('.') + 1))
            .toList();
    }
}
