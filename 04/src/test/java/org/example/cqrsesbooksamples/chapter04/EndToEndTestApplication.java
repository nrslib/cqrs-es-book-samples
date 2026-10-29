package org.example.cqrsesbooksamples.chapter04;

import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 倉庫（_20）・注文（_29）・シーケンシャルサガ（_32）を組み合わせた
 * エンドツーエンド検証用のアプリケーション。
 * _27/_28のコントローラ変形や_33の並列サガは含めない。
 */
@SpringBootApplication(scanBasePackageClasses = {
        org.example.cqrsesbooksamples.chapter04._20.ProductInventoryProjection.class,
        org.example.cqrsesbooksamples.chapter04._29.OrderAggregateAdapter.class,
        org.example.cqrsesbooksamples.chapter04._32.OrderProcessingSaga.class
})
public class EndToEndTestApplication {
    /**
     * サガの状態はJPAサガストアに汎用シリアライザで永続化される。
     * privateフィールドを読めるようフィールド可視性を広げたJacksonを使う
     * （XStreamはJDKの制約でrecordを直列化できない）。
     */
    @Bean
    @Primary
    public Serializer serializer() {
        ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return JacksonSerializer.builder().objectMapper(mapper).build();
    }
}
