// 検証補助: 4.5
package org.example.cqrsesbooksamples.chapter04._01;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

// DocumentViewの集計はfindById→加算→saveの読み書きで更新するため、
// 複数セグメントで並列処理すると同一行の更新が競合しうる。
// このテストは集計の畳み込み自体を観測する目的なので1セグメントに固定する
@SpringBootTest(properties = {
        "axon.eventhandling.processors[org.example.cqrsesbooksamples.chapter04._01].initial-segment-count=1"
})
class DocumentHelpfulIntegrationTest {
    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private DocumentQueryController documentQueryController;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 評価の集計がリードモデルへ畳み込まれる() {
        String documentId = commandGateway.sendAndWait(
            new CreateDocumentCommand("CQRS入門", "本文"));

        // 2人のユーザーが「役に立った」を付ける
        commandGateway.sendAndWait(new MarkAsHelpfulCommand(documentId, "user-1"));
        commandGateway.sendAndWait(new MarkAsHelpfulCommand(documentId, "user-2"));

        await().ignoreExceptions().untilAsserted(() -> {
            var view = documentQueryController.get(documentId);
            assertThat(view.getHelpfulCount()).isEqualTo(2);
            assertThat(view.getTitle()).isEqualTo("CQRS入門");
        });

        // 1人が評価を外すと集計も減る
        commandGateway.sendAndWait(new UnmarkAsHelpfulCommand(documentId, "user-1"));

        await().ignoreExceptions().untilAsserted(() -> {
            var view = documentQueryController.get(documentId);
            assertThat(view.getHelpfulCount()).isEqualTo(1);
        });

        // 同じユーザーが評価を付け直す
        commandGateway.sendAndWait(new MarkAsHelpfulCommand(documentId, "user-1"));

        // ドキュメントとユーザーの複合識別子のストリームへ、評価の変更履歴が積まれる
        var events = jdbcTemplate.queryForList(
            "SELECT payload_type, sequence_number FROM domain_event_entry "
                + "WHERE aggregate_identifier = ? ORDER BY sequence_number",
            documentId + "_user-1");
        assertThat(events).hasSize(3);
        assertThat((String) events.get(0).get("payload_type")).endsWith("HelpfulMarkedEvent");
        assertThat((String) events.get(1).get("payload_type")).endsWith("HelpfulUnmarkedEvent");
        assertThat((String) events.get(2).get("payload_type")).endsWith("HelpfulMarkedEvent");
        assertThat(events)
            .extracting(event -> ((Number) event.get("sequence_number")).longValue())
            .containsExactly(0L, 1L, 2L);
    }
}
