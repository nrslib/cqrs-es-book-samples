// 検証補助: 3.28, 3.29
package org.example.cqrsesbooksamples.chapter03._21;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.stream.Stream;

import org.axonframework.eventhandling.GenericDomainEventEntry;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.serialization.upcasting.event.InitialEventRepresentation;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

class UserRegisteredEventUpcasterTest {
    private final JacksonSerializer serializer = JacksonSerializer.defaultSerializer();

    // 保存済みのVersion 1.0データが読み込み時にVersion 2.0へ変換される
    @Test
    void バージョン1のイベントがバージョン2へ変換される() {
        var payload = """
            {"userId":"user-123","email":"user@example.com","password":"hashedPassword123"}
            """.getBytes(StandardCharsets.UTF_8);
        var entry = new GenericDomainEventEntry<>(
            "UserAggregate", "user-123", 0, "event-1", Instant.now(),
            "com.example.UserRegisteredEvent", "1.0", payload, "{}".getBytes(StandardCharsets.UTF_8));
        var representation = new InitialEventRepresentation(entry, serializer);

        var upcaster = new UserRegisteredEventUpcaster();
        var upcasted = upcaster.upcast(Stream.of(representation)).findFirst().orElseThrow();

        assertThat(upcasted.getType().getName()).isEqualTo("com.example.UserRegisteredEvent");
        assertThat(upcasted.getType().getRevision()).isEqualTo("2.0");

        JsonNode node = upcasted.getData(JsonNode.class).getData();
        assertThat(node.get("passwordHash").asText()).isEqualTo("hashedPassword123");
        assertThat(node.has("password")).isFalse();
        assertThat(node.get("birthDate").asText()).isEqualTo("1900-01-01");
    }

    @Test
    void チェーンを通すとバージョン1が1_1を経由して2へ変換される() {
        var payload = """
            {"userId":"user-123","email":"user@example.com","password":"hashedPassword123"}
            """.getBytes(StandardCharsets.UTF_8);
        var entry = new GenericDomainEventEntry<>(
            "UserAggregate", "user-123", 0, "event-1", Instant.now(),
            "com.example.UserRegisteredEvent", "1.0", payload, "{}".getBytes(StandardCharsets.UTF_8));
        var representation = new InitialEventRepresentation(entry, serializer);

        var chain = new org.axonframework.serialization.upcasting.event.EventUpcasterChain(
            new UserRegisteredEventV1ToV1_1Upcaster(),
            new UserRegisteredEventV1_1ToV2Upcaster());
        var upcasted = chain.upcast(Stream.of(representation)).findFirst().orElseThrow();

        assertThat(upcasted.getType().getRevision()).isEqualTo("2.0");
        JsonNode node = upcasted.getData(JsonNode.class).getData();
        assertThat(node.get("passwordHash").asText()).isEqualTo("hashedPassword123");
    }
}
