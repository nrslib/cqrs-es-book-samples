// 検証補助: 2.12, 2.13, 2.45, 2.46
package org.example.cqrsesbooksamples.chapter02._17;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.modelling.command.ConcurrencyException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * イベントストアに保存されるデータとシーケンス番号まわりの転記リストを実際に動かして検証する。
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter02_17;DB_CLOSE_DELAY=-1")
class EventStoreSequenceIntegrationTest {
    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private EventStore eventStore;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    void イベントストアの1行は識別子とシーケンス番号と型とペイロードを保持する() {
        String userId = commandGateway.sendAndWait(new CreateUserCommand("John", "john@example.com"));

        var row = jdbcTemplate.queryForMap(
            "SELECT global_index, event_identifier, payload_revision, "
                + "payload_type, time_stamp, aggregate_identifier, sequence_number, type "
                + "FROM domain_event_entry WHERE aggregate_identifier = ?",
            userId);

        // グローバル連番・イベント識別子・発生時刻が採番されている
        assertThat(((Number) row.get("global_index")).longValue()).isPositive();
        assertThat((String) row.get("event_identifier")).isNotBlank();
        assertThat((String) row.get("time_stamp")).isNotBlank();

        // アグリゲートの識別子・順序・型が記録されている
        assertThat(row.get("aggregate_identifier")).isEqualTo(userId);
        assertThat(((Number) row.get("sequence_number")).longValue()).isEqualTo(0L);
        assertThat(row.get("type")).isEqualTo("UserAggregate");
        assertThat((String) row.get("payload_type")).endsWith("UserCreatedEvent");

        // ペイロードはイベントの実データ、メタデータはtraceId等の付帯情報を持つ
        byte[] payloadBytes = jdbcTemplate.queryForObject(
            "SELECT payload FROM domain_event_entry WHERE aggregate_identifier = ?",
            byte[].class, userId);
        assertThat(new String(payloadBytes, StandardCharsets.UTF_8))
            .contains("\"name\":\"John\"").contains("\"email\":\"john@example.com\"");
        byte[] metaDataBytes = jdbcTemplate.queryForObject(
            "SELECT meta_data FROM domain_event_entry WHERE aggregate_identifier = ?",
            byte[].class, userId);
        assertThat(new String(metaDataBytes, StandardCharsets.UTF_8))
            .contains("traceId").contains("correlationId");
    }

    // 削除済み（旧 2.57）
    @Test
    void イベントはシーケンス番号順に保存されシーケンス番号順に取得できる() {
        String orderId = "order-123";
        eventStore.publish(new GenericDomainEventMessage<>(
            "OrderAggregate", orderId, 0, new OrderPlacedEvent(orderId, 1000)));
        eventStore.publish(new GenericDomainEventMessage<>(
            "OrderAggregate", orderId, 1, new OrderItemAddedEvent(orderId, "item-1")));
        eventStore.publish(new GenericDomainEventMessage<>(
            "OrderAggregate", orderId, 2, new OrderConfirmedEvent(orderId)));
        eventStore.publish(new GenericDomainEventMessage<>(
            "OrderAggregate", orderId, 3, new OrderShippedEvent(orderId)));

        var rows = jdbcTemplate.queryForList(
            "SELECT aggregate_identifier, sequence_number, payload_type, time_stamp "
                + "FROM domain_event_entry "
                + "WHERE aggregate_identifier = ? "
                + "ORDER BY sequence_number",
            orderId);

        assertThat(rows).hasSize(4);
        assertThat(rows)
            .extracting(row -> ((Number) row.get("sequence_number")).longValue())
            .containsExactly(0L, 1L, 2L, 3L);
        assertThat(rows)
            .extracting(row -> simplePayloadType((String) row.get("payload_type")))
            .containsExactly(
                "OrderPlacedEvent",
                "OrderItemAddedEvent",
                "OrderConfirmedEvent",
                "OrderShippedEvent");
        assertThat(rows)
            .allSatisfy(row -> assertThat((String) row.get("time_stamp")).isNotBlank());

        // ORDER BY sequence_number ASC で読み戻すと発行順に並ぶ
        var events = eventStore.readEvents(orderId).asStream().toList();
        assertThat(events).hasSize(4);
        assertThat(events.get(0).getSequenceNumber()).isEqualTo(0L);
        assertThat(events.get(3).getSequenceNumber()).isEqualTo(3L);
    }


    @Test
    void 同じシーケンス番号のイベントはユニーク制約違反で保存に失敗する() {
        String userId = commandGateway.sendAndWait(new CreateUserCommand("Jane", "jane@example.com"));
        commandGateway.sendAndWait(new UpdateUserCommand(userId, "Jane Doe", "jane.doe@example.com"));

        // 現在の最新はシーケンス番号1。同じ番号での保存はユニーク制約違反になる
        var conflicting = new GenericDomainEventMessage<>(
            "UserAggregate", userId, 1,
            new UserUpdatedEvent(userId, "競合", "conflict@example.com"));

        assertThatThrownBy(() -> eventStore.publish(conflicting))
            .isInstanceOf(ConcurrencyException.class);
    }

    private String simplePayloadType(String payloadType) {
        int separator = Math.max(payloadType.lastIndexOf('.'), payloadType.lastIndexOf('$'));
        return payloadType.substring(separator + 1);
    }
}
