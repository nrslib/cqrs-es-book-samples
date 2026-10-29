// 検証補助: 3.36
package org.example.cqrsesbooksamples.chapter03._32;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.axonframework.serialization.SimpleSerializedObject;
import org.axonframework.serialization.json.JacksonSerializer;
import org.junit.jupiter.api.Test;

/**
 * 個人情報を含むイベント（リスト3.36）の検証。
 * 本書標準のJacksonシリアライザでイベントストアへの保存・復元と
 * 同じ直列化の往復ができることを確かめる。
 */
class UserRegisteredEventSerializationTest {

    @Test
    void Jacksonで直列化して復元できる() {
        var serializer = JacksonSerializer.defaultSerializer();
        var event = new UserRegisteredEvent(
                "user-1", "John", "john@example.com",
                "090-0000-0000", "Tokyo", LocalDate.of(1990, 1, 1));

        var serialized = serializer.serialize(event, byte[].class);
        var restored = serializer.deserialize(new SimpleSerializedObject<>(
                serialized.getData(), byte[].class, serialized.getType()));

        assertThat(restored).isEqualTo(event);
    }
}
