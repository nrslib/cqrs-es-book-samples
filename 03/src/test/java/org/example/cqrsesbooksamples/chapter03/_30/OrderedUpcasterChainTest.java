// 検証補助: 3.34
package org.example.cqrsesbooksamples.chapter03._30;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.stream.Stream;

import org.axonframework.eventhandling.GenericDomainEventEntry;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.serialization.upcasting.event.EventUpcaster;
import org.axonframework.serialization.upcasting.event.EventUpcasterChain;
import org.axonframework.serialization.upcasting.event.InitialEventRepresentation;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;


class OrderedUpcasterChainTest {

    @Test
    void Orderの指定順でRevision2まで変換される() {
        var upcasters = new ArrayList<EventUpcaster>();
        upcasters.add(new UserRegisteredEventUpcasterV1ToV2());
        upcasters.add(new UserRegisteredEventUpcasterToV1());
        AnnotationAwareOrderComparator.sort(upcasters);

        var entry = new GenericDomainEventEntry<>(
            "UserAggregate",
            "user-123",
            0,
            "event-1",
            Instant.parse("2026-01-01T00:00:00Z"),
            "com.example.UserRegisteredEvent",
            null,
            "{\"userId\":\"user-123\"}".getBytes(StandardCharsets.UTF_8),
            "{}".getBytes(StandardCharsets.UTF_8)
        );
        var representation = new InitialEventRepresentation(
            entry,
            JacksonSerializer.defaultSerializer()
        );
        var chain = new EventUpcasterChain(upcasters);

        var upcasted = chain.upcast(Stream.of(representation)).findFirst().orElseThrow();

        assertThat(upcasted.getType().getRevision()).isEqualTo("2");
    }
}
