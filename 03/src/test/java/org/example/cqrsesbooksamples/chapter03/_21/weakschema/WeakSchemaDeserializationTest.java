// 検証補助: 3.35
package org.example.cqrsesbooksamples.chapter03._21.weakschema;

import static org.assertj.core.api.Assertions.assertThat;

import org.example.cqrsesbooksamples.chapter03._21.weakschema.after.UserRegisteredEvent;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * リスト3.35のWeak Schema前提を検証する。
 * 同じクラス名のまま任意フィールドを追加すれば、旧形式のJSONは
 * 追加フィールドがnullのままデシリアライズできる。
 */
class WeakSchemaDeserializationTest {

    @Test
    void 旧形式のJSONは追加フィールドがnullのまま新しい定義で読める() throws Exception {
        var oldJson = "{\"userId\":\"user-123\",\"email\":\"user@example.com\"}";

        var event = new ObjectMapper().readValue(oldJson, UserRegisteredEvent.class);

        assertThat(event.userId()).isEqualTo("user-123");
        assertThat(event.email()).isEqualTo("user@example.com");
        assertThat(event.phoneNumber()).isNull();
    }
}
