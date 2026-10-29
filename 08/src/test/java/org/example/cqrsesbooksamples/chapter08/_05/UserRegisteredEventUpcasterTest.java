// 紙面照合: 8.5, 8.6
package org.example.cqrsesbooksamples.chapter08._05;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.stream.Stream;

import org.axonframework.eventhandling.GenericDomainEventEntry;
import org.axonframework.serialization.SimpleSerializedType;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.serialization.upcasting.event.EventUpcasterChain;
import org.axonframework.serialization.upcasting.event.InitialEventRepresentation;
import org.example.cqrsesbooksamples.chapter03._26.UserRegisteredEventUpcaster;
import org.example.cqrsesbooksamples.chapter03._30.UserRegisteredEventUpcasterToV1;
import org.example.cqrsesbooksamples.chapter03._30.UserRegisteredEventUpcasterV1ToV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;


public class UserRegisteredEventUpcasterTest {
    private UserRegisteredEventUpcaster upcaster;
    private JacksonSerializer serializer;

    @BeforeEach
    void setUp() {
        upcaster = new UserRegisteredEventUpcaster();
        serializer = JacksonSerializer.defaultSerializer();
    }


    @Test
    void shouldUpcastLegacyEventToRevision1() {
        // 旧payload(リビジョン未付与)を組み立てる
        var legacyPayload = JsonNodeFactory.instance.objectNode()
            .put("userId", "U-1")
            .put("name", "Alice");

        var legacyType = new SimpleSerializedType(
            "com.example.UserRegisteredEvent",
            null  // リビジョン未付与
        );
        var representation = new InitialEventRepresentation(
            new GenericDomainEventEntry<>(
                "UserAggregate", "U-1", 0,
                "msg-1",
                Instant.now(),
                legacyType.getName(),
                legacyType.getRevision(),
                serializer.serialize(legacyPayload, byte[].class).getData(),
                new byte[0]
            ),
            serializer
        );

        // Upcasterに通して変換結果を取り出す
        var upcasted = upcaster.upcast(Stream.of(representation))
            .findFirst()
            .orElseThrow();
        var upcastedPayload = upcasted
            .getData(JsonNode.class)
            .getData();

        assertEquals("U-1", upcastedPayload.get("userId").asText());
        assertEquals("Alice", upcastedPayload.get("name").asText());
        assertEquals("", upcastedPayload.get("email").asText());
        assertEquals("1", upcasted.getType().getRevision());
    }


    @Test
    void shouldChainUpcastersFromLegacyToV2() {
        var chain = new EventUpcasterChain(
            new UserRegisteredEventUpcasterToV1(),
            new UserRegisteredEventUpcasterV1ToV2()
        );

        var legacyRepresentation = buildLegacyRepresentation();  // 補助メソッドで組み立て

        var upcastedStream = chain.upcast(Stream.of(legacyRepresentation));
        var upcasted = upcastedStream.findFirst().orElseThrow();

        // チェーン出口のリビジョンが最新バージョンになっていること
        assertEquals("2", upcasted.getType().getRevision());
    }

    private InitialEventRepresentation buildLegacyRepresentation() {
        var legacyPayload = JsonNodeFactory.instance.objectNode()
            .put("userId", "U-1")
            .put("name", "Alice");
        return new InitialEventRepresentation(
            new GenericDomainEventEntry<>(
                "UserAggregate", "U-1", 0,
                "msg-1",
                Instant.now(),
                "com.example.UserRegisteredEvent",
                null,
                serializer.serialize(legacyPayload, byte[].class).getData(),
                new byte[0]
            ),
            serializer
        );
    }
}
