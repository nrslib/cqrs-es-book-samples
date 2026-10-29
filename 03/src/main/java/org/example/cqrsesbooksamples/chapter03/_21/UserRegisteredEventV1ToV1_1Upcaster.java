// 検証補助: 3.33
package org.example.cqrsesbooksamples.chapter03._21;

import org.axonframework.serialization.SimpleSerializedType;
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation;
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster;

import com.fasterxml.jackson.databind.JsonNode;


public class UserRegisteredEventV1ToV1_1Upcaster extends SingleEventUpcaster {
    private static final SimpleSerializedType TARGET_TYPE =
            new SimpleSerializedType("com.example.UserRegisteredEvent", "1.0");

    @Override
    protected boolean canUpcast(IntermediateEventRepresentation representation) {
        return TARGET_TYPE.equals(representation.getType());
    }

    @Override
    protected IntermediateEventRepresentation doUpcast(IntermediateEventRepresentation representation) {
        return representation.upcastPayload(
            new SimpleSerializedType("com.example.UserRegisteredEvent", "1.1"),
            JsonNode.class,
            node -> node
        );
    }
}
