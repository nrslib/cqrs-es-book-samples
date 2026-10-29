package org.example.cqrsesbooksamples.chapter03._26;

import org.axonframework.serialization.SimpleSerializedType;
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation;
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

// 削除済み（旧 3.44）
@Component
public class UserRegisteredEventUpcaster extends SingleEventUpcaster {

    @Override
    protected boolean canUpcast(IntermediateEventRepresentation representation) {
        // Revision 未付与の旧イベントを変換対象とする
        return "com.example.UserRegisteredEvent".equals(representation.getType().getName())
            && representation.getType().getRevision() == null;
    }

    @Override
    protected IntermediateEventRepresentation doUpcast(IntermediateEventRepresentation representation) {
        // JSONペイロードに新しいフィールドを追加して、@Revision("1") のイベントへ変換
        return representation.upcastPayload(
            new SimpleSerializedType("com.example.UserRegisteredEvent", "1"),
            JsonNode.class,
            node -> {
                ObjectNode objNode = (ObjectNode) node;
                // メールアドレスフィールドがなければ空文字列で追加
                if (!objNode.has("email")) {
                    objNode.put("email", "");
                }
                return objNode;
            }
        );
    }
}
