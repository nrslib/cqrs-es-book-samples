// 検証補助: 3.40
package org.example.cqrsesbooksamples.chapter03._35.serializer;

import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.axonframework.serialization.Converter;
import org.axonframework.serialization.SerializationException;
import org.axonframework.serialization.SerializedObject;
import org.axonframework.serialization.SerializedType;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.SimpleSerializedObject;
import org.axonframework.serialization.json.JacksonSerializer;

/** 名前を変更していないrecord直下のStringフィールドを対象にした例。 */
public final class FieldEncryptingSerializer implements Serializer {
    private final JacksonSerializer delegate;
    private final ObjectMapper mapper;
    private final CryptoEngine cryptoEngine;

    public FieldEncryptingSerializer(JacksonSerializer delegate, CryptoEngine cryptoEngine) {
        this.delegate = delegate;
        this.mapper = delegate.getObjectMapper();
        this.cryptoEngine = cryptoEngine;
    }

    @Override
    public <T> SerializedObject<T> serialize(Object object, Class<T> expectedRepresentation) {
        Fields fields = fieldsOf(object == null ? null : object.getClass());
        if (fields == null) {
            return delegate.serialize(object, expectedRepresentation);
        }
        SerializedObject<JsonNode> serialized = delegate.serialize(object, JsonNode.class);
        ObjectNode data = objectNode(serialized.getData());
        String subjectId = subjectId(data, fields);
        for (String field : fields.personal()) {
            JsonNode plain = data.required(field);
            if (!plain.isTextual() && !plain.isNull()) {
                throw new SerializationException("個人情報のフィールドはString型に限定しています");
            }
            var encrypted = cryptoEngine.encrypt(subjectId,
                    context(serialized.getType(), subjectId, field), plain.toString());
            data.set(field, mapper.valueToTree(encrypted));
        }
        SerializedObject<T> converted = delegate.serialize(data, expectedRepresentation);
        return new SimpleSerializedObject<>(converted.getData(), expectedRepresentation, serialized.getType());
    }

    @Override
    public <S, T> T deserialize(SerializedObject<S> serialized) {
        Fields fields = fieldsOf(delegate.classForType(serialized.getType()));
        if (fields == null) {
            return delegate.deserialize(serialized);
        }
        ObjectNode data = objectNode(delegate.getConverter().convert(serialized, JsonNode.class).getData());
        String subjectId = subjectId(data, fields);
        for (String field : fields.personal()) {
            JsonNode value = data.required(field);
            if (!value.isObject()) {
                throw new SerializationException("個人情報のフィールドに暗号文が必要です");
            }
            try {
                var encrypted = mapper.treeToValue(value, CryptoEngine.EncryptedValue.class);
                String plain = cryptoEngine.decrypt(subjectId,
                        context(serialized.getType(), subjectId, field), encrypted);
                if (plain == null) {
                    data.putNull(field);
                } else {
                    JsonNode restored = mapper.readTree(plain);
                    if (restored == null || (!restored.isTextual() && !restored.isNull())) {
                        throw new SerializationException("復号したフィールドの型が不正です");
                    }
                    data.set(field, restored);
                }
            } catch (IOException e) {
                throw new SerializationException("暗号化フィールドの読み出しに失敗しました", e);
            }
        }
        return delegate.deserialize(new SimpleSerializedObject<>(data, JsonNode.class, serialized.getType()));
    }

    private Fields fieldsOf(Class<?> type) {
        if (type == null || !type.isRecord()) {
            return null;
        }
        String subject = null;
        List<String> personal = new ArrayList<>();
        for (RecordComponent component : type.getRecordComponents()) {
            boolean isSubject = component.isAnnotationPresent(DataSubjectId.class);
            boolean isPersonal = component.isAnnotationPresent(PersonalData.class);
            if ((isSubject || isPersonal) && component.getType() != String.class) {
                throw new SerializationException("暗号化用アノテーションはString型に指定してください");
            }
            if (isSubject) {
                if (subject != null || isPersonal) {
                    throw new SerializationException("主体IDは暗号化しない単一のフィールドに指定してください");
                }
                subject = component.getName();
            }
            if (isPersonal) {
                personal.add(component.getName());
            }
        }
        if (personal.isEmpty()) {
            return null;
        }
        if (subject == null) {
            throw new SerializationException("個人情報を暗号化するには主体IDが必要です");
        }
        return new Fields(subject, List.copyOf(personal));
    }

    private ObjectNode objectNode(JsonNode data) {
        if (!(data instanceof ObjectNode object)) {
            throw new SerializationException("イベントのJSONオブジェクトが必要です");
        }
        return object.deepCopy();
    }

    private String subjectId(ObjectNode data, Fields fields) {
        JsonNode subject = data.required(fields.subject());
        if (!subject.isTextual() || subject.textValue().isBlank()) {
            throw new SerializationException("空でない主体IDが必要です");
        }
        return subject.textValue();
    }

    private String context(SerializedType type, String subjectId, String field) {
        // イベント型・主体・フィールド間で暗号文を取り違えないよう認証する。
        return mapper.valueToTree(List.of(type.getName(), subjectId, field)).toString();
    }

    @Override
    public <T> boolean canSerializeTo(Class<T> expectedRepresentation) {
        return delegate.canSerializeTo(expectedRepresentation);
    }

    @Override
    public Class<?> classForType(SerializedType type) {
        return delegate.classForType(type);
    }

    @Override
    public SerializedType typeForClass(Class type) {
        return delegate.typeForClass(type);
    }

    @Override
    public Converter getConverter() {
        return delegate.getConverter();
    }

    private record Fields(String subject, List<String> personal) {
    }
}
