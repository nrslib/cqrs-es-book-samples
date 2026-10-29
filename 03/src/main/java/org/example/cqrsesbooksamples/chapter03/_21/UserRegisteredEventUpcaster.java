// 紙面照合: 3.30
package org.example.cqrsesbooksamples.chapter03._21;

import java.time.LocalDate;

import org.axonframework.serialization.SimpleSerializedType;
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation;
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class UserRegisteredEventUpcaster extends SingleEventUpcaster {
    private static final SimpleSerializedType TARGET_TYPE =
            new SimpleSerializedType(
                    "com.example.UserRegisteredEvent",
                    "1.0"  // 変換対象となる古いバージョン
            );

    @Override
    protected boolean canUpcast(IntermediateEventRepresentation representation) {
        // このUpcasterが処理すべきイベントかを判定
        return TARGET_TYPE.equals(representation.getType());
    }

    @Override
    protected IntermediateEventRepresentation doUpcast(IntermediateEventRepresentation representation) {
        // JSONペイロードを書き換えて、バージョン2.0のイベントへ変換
        return representation.upcastPayload(
            new SimpleSerializedType("com.example.UserRegisteredEvent", "2.0"),
            JsonNode.class,
            node -> {
                ObjectNode objNode = (ObjectNode) node;

                // パスワードフィールドの名前を変更
                // 古い"password"フィールドを新しい"passwordHash"に変換
                objNode.set("passwordHash", objNode.remove("password"));

                // デフォルトの誕生日を追加
                // 既存ユーザには仮の誕生日を設定（後で更新を促す）
                objNode.put("birthDate", LocalDate.of(1900, 1, 1).toString());

                return objNode;
            }
        );
    }
}
