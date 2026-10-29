// 紙面照合: 3.40
package org.example.cqrsesbooksamples.chapter03._35.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CryptoConfiguration {
    @Bean
    public InMemoryCryptoEngine cryptoEngine() {
        return new InMemoryCryptoEngine();
    }

    @Bean
    @Qualifier("eventSerializer")
    public Serializer eventSerializer(ObjectMapper objectMapper, CryptoEngine cryptoEngine) {
        var jackson = JacksonSerializer.builder().objectMapper(objectMapper).build();
        return new FieldEncryptingSerializer(jackson, cryptoEngine);
    }
}
