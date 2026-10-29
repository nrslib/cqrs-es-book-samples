// 紙面照合: 3.34
package org.example.cqrsesbooksamples.chapter03._30;

import org.axonframework.serialization.SimpleSerializedType;
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation;
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;


@Component
@Order(2)
public class UserRegisteredEventUpcasterV1ToV2 extends SingleEventUpcaster {
    // Revision 1 から Revision 2 への変換

    @Override
    protected boolean canUpcast(IntermediateEventRepresentation representation) {
        return "com.example.UserRegisteredEvent".equals(representation.getType().getName())
            && "1".equals(representation.getType().getRevision());
    }

    @Override
    protected IntermediateEventRepresentation doUpcast(IntermediateEventRepresentation representation) {
        return representation.upcastPayload(
            new SimpleSerializedType("com.example.UserRegisteredEvent", "2"),
            JsonNode.class,
            node -> node
        );
    }
}
