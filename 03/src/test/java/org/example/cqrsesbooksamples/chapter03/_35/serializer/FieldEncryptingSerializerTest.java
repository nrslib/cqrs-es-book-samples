// 検証補助: 3.40
package org.example.cqrsesbooksamples.chapter03._35.serializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.axonframework.messaging.MetaData;
import org.axonframework.serialization.Revision;
import org.axonframework.serialization.SerializationException;
import org.axonframework.serialization.SerializedObject;
import org.axonframework.serialization.SimpleSerializedObject;
import org.axonframework.serialization.json.JacksonSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FieldEncryptingSerializerTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final InMemoryCryptoEngine engine = new InMemoryCryptoEngine();
    private final JacksonSerializer jackson = JacksonSerializer.builder().objectMapper(mapper).build();
    private final FieldEncryptingSerializer serializer = new FieldEncryptingSerializer(jackson, engine);
    private final UserRegisteredEvent event = new UserRegisteredEvent("user-1", "太郎", "taro@example.com");

    @BeforeEach
    void prepareKey() {
        engine.createKey(event.userId());
    }

    @Test
    void 個人情報だけを暗号化して型とリビジョンを保って往復できる() throws Exception {
        var versioned = new VersionedEvent("user-1", "太郎", "2026-09-25T00:00:00Z");
        var serialized = serializer.serialize(versioned, byte[].class);
        JsonNode payload = mapper.readTree(serialized.getData());

        assertThat(payload.path("userId").asText()).isEqualTo("user-1");
        assertThat(payload.path("registeredAt").asText()).isEqualTo(versioned.registeredAt());
        assertThat(payload.path("name").isObject()).isTrue();
        assertThat(payload.path("name").toString()).doesNotContain(versioned.name());
        assertThat(serialized.getType().getName()).isEqualTo(VersionedEvent.class.getName());
        assertThat(serialized.getType().getRevision()).isEqualTo("2");
        assertThat(serializer.<byte[], VersionedEvent>deserialize(serialized)).isEqualTo(versioned);
        assertThat(serializer.<String, UserRegisteredEvent>deserialize(serializer.serialize(event, String.class)))
                .isEqualTo(event);
        assertThat(serializer.<JsonNode, UserRegisteredEvent>deserialize(serializer.serialize(event, JsonNode.class)))
                .isEqualTo(event);
    }

    @Test
    void 正式な鍵破棄後はIDと保存済み暗号文を保ち個人情報だけを消去済みにする() {
        var serialized = serializer.serialize(event, String.class);
        String persisted = serialized.getData();
        engine.destroyKey(event.userId());
        engine.destroyKey(event.userId());

        UserRegisteredEvent restored = serializer.deserialize(serialized);

        assertThat(restored).isEqualTo(new UserRegisteredEvent(event.userId(), null, null));
        assertThat(serialized.getData()).isEqualTo(persisted);
        assertThatThrownBy(() -> serializer.serialize(event, String.class)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> engine.createKey(event.userId())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 破棄記録も鍵もない場合は消去済みとして扱わない() {
        var serialized = serializer.serialize(event, String.class);
        var withoutKeys = new FieldEncryptingSerializer(jackson, new InMemoryCryptoEngine());
        assertThatThrownBy(() -> withoutKeys.deserialize(serialized)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 鍵取得障害を消去済みとして扱わず呼び出し元へ伝える() {
        var serialized = serializer.serialize(event, String.class);
        var unavailable = spy(engine);
        var failure = new IllegalStateException("鍵管理サービスに接続できません");
        doThrow(failure).when(unavailable).decrypt(anyString(), anyString(), any());
        var reader = new FieldEncryptingSerializer(jackson, unavailable);
        assertThatThrownBy(() -> reader.deserialize(serialized)).isSameAs(failure);
    }

    @Test
    void フィールド間で暗号文を入れ替えると認証に失敗する() {
        var serialized = serializer.serialize(event, JsonNode.class);
        ObjectNode changed = serialized.getData().deepCopy();
        JsonNode name = changed.get("name");
        changed.set("name", changed.get("email"));
        changed.set("email", name);
        assertThatThrownBy(() -> serializer.deserialize(replaceData(serialized, changed)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 別の主体の暗号文には置き換えられない() {
        engine.createKey("user-2");
        var other = serializer.serialize(new UserRegisteredEvent("user-2", "次郎", "jiro@example.com"), JsonNode.class);
        var serialized = serializer.serialize(event, JsonNode.class);
        ObjectNode changed = serialized.getData().deepCopy();
        changed.set("name", other.getData().get("name"));
        assertThatThrownBy(() -> serializer.deserialize(replaceData(serialized, changed)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 暗号文の改ざんは失敗になる() {
        var serialized = serializer.serialize(event, JsonNode.class);
        ObjectNode changed = serialized.getData().deepCopy();
        ObjectNode name = (ObjectNode) changed.get("name");
        String ciphertext = name.get("ciphertext").asText();
        name.put("ciphertext", (ciphertext.startsWith("A") ? "B" : "A") + ciphertext.substring(1));
        assertThatThrownBy(() -> serializer.deserialize(replaceData(serialized, changed)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 平文への置き換えを読み飛ばさない() {
        var serialized = serializer.serialize(event, JsonNode.class);
        ObjectNode changed = serialized.getData().deepCopy();
        changed.put("name", "平文");
        assertThatThrownBy(() -> serializer.deserialize(replaceData(serialized, changed)))
                .isInstanceOf(SerializationException.class);
    }

    @Test
    void 個人情報アノテーションのないメタデータは通常どおり扱う() {
        var metadata = MetaData.from(Map.of("traceId", "trace-1"));
        var serialized = serializer.serialize(metadata, byte[].class);
        assertThat(serializer.<byte[], MetaData>deserialize(serialized)).isEqualTo(metadata);
    }

    @Test
    void 主体IDの指定漏れは平文保存せず失敗する() {
        assertThatThrownBy(() -> serializer.serialize(new MissingSubject("太郎"), String.class))
                .isInstanceOf(SerializationException.class);
    }

    @Test
    void 元からnullの個人情報も暗号化して往復する() {
        var nullable = new UserRegisteredEvent("user-1", null, null);
        var serialized = serializer.serialize(nullable, JsonNode.class);
        assertThat(serialized.getData().path("name").isObject()).isTrue();
        assertThat(serializer.<JsonNode, UserRegisteredEvent>deserialize(serialized)).isEqualTo(nullable);
    }

    private SerializedObject<JsonNode> replaceData(SerializedObject<JsonNode> original, JsonNode data) {
        return new SimpleSerializedObject<>(data, JsonNode.class, original.getType());
    }

    @Revision("2")
    public record VersionedEvent(@DataSubjectId String userId, @PersonalData String name, String registeredAt) {
    }

    public record MissingSubject(@PersonalData String name) {
    }
}
