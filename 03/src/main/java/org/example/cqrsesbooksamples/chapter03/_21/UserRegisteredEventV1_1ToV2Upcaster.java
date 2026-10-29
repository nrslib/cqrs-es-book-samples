// 検証補助: 3.33
package org.example.cqrsesbooksamples.chapter03._21;

import java.time.LocalDate;

import org.axonframework.serialization.SimpleSerializedType;
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation;
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class UserRegisteredEventV1_1ToV2Upcaster extends SingleEventUpcaster {
    private static final SimpleSerializedType TARGET_TYPE =
            new SimpleSerializedType("com.example.UserRegisteredEvent", "1.1");

    @Override
    protected boolean canUpcast(IntermediateEventRepresentation representation) {
        return TARGET_TYPE.equals(representation.getType());
    }

    @Override
    protected IntermediateEventRepresentation doUpcast(IntermediateEventRepresentation representation) {
        return representation.upcastPayload(
            new SimpleSerializedType("com.example.UserRegisteredEvent", "2.0"),
            JsonNode.class,
            node -> {
                ObjectNode objNode = (ObjectNode) node;
                objNode.set("passwordHash", objNode.remove("password"));
                objNode.put("birthDate", LocalDate.of(1900, 1, 1).toString());
                return objNode;
            }
        );
    }
}
