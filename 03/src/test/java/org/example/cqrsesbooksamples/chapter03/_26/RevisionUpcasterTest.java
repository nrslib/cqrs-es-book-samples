package org.example.cqrsesbooksamples.chapter03._26;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.stream.Stream;

import org.axonframework.eventhandling.GenericDomainEventEntry;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.serialization.upcasting.event.InitialEventRepresentation;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

class RevisionUpcasterTest {
    private final JacksonSerializer serializer = JacksonSerializer.defaultSerializer();

    @Test
    void Revision未付与の旧イベントがRevision1へ変換される() {
        var payload = """
            {"userId":"user-123","name":"John"}
            """.getBytes(StandardCharsets.UTF_8);
        var entry = new GenericDomainEventEntry<>(
            "UserAggregate", "user-123", 0, "event-1", Instant.now(),
            "com.example.UserRegisteredEvent", null, payload, "{}".getBytes(StandardCharsets.UTF_8));
        var representation = new InitialEventRepresentation(entry, serializer);

        var upcaster = new UserRegisteredEventUpcaster();
        var upcasted = upcaster.upcast(Stream.of(representation)).findFirst().orElseThrow();

        assertThat(upcasted.getType().getRevision()).isEqualTo("1");
        JsonNode node = upcasted.getData(JsonNode.class).getData();
        assertThat(node.get("email").asText()).isEmpty();
        assertThat(node.get("name").asText()).isEqualTo("John");
    }

    @Test
    void Revision付きの新イベントは変換対象にならない() {
        var payload = """
            {"userId":"user-123","name":"John","email":"john@example.com"}
            """.getBytes(StandardCharsets.UTF_8);
        var entry = new GenericDomainEventEntry<>(
            "UserAggregate", "user-123", 0, "event-1", Instant.now(),
            "com.example.UserRegisteredEvent", "1", payload, "{}".getBytes(StandardCharsets.UTF_8));
        var representation = new InitialEventRepresentation(entry, serializer);

        var upcaster = new UserRegisteredEventUpcaster();
        var upcasted = upcaster.upcast(Stream.of(representation)).findFirst().orElseThrow();

        assertThat(upcasted.getType().getRevision()).isEqualTo("1");
        JsonNode node = upcasted.getData(JsonNode.class).getData();
        assertThat(node.get("email").asText()).isEqualTo("john@example.com");
    }
}
