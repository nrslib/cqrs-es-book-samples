// 検証補助: 2.8
package org.example.cqrsesbooksamples.chapter02._9;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.CreationPolicy;
import org.junit.jupiter.api.Test;

class CreationPolicyTypesTest {


    @Test
    void CreationPolicyのみっつの種類を指定できる() throws Exception {
        assertThat(policyOf("always")).isEqualTo(AggregateCreationPolicy.ALWAYS);
        assertThat(policyOf("createIfMissing")).isEqualTo(AggregateCreationPolicy.CREATE_IF_MISSING);
        assertThat(policyOf("never")).isEqualTo(AggregateCreationPolicy.NEVER);
    }

    private AggregateCreationPolicy policyOf(String methodName) throws Exception {
        Method method = Handlers.class.getDeclaredMethod(methodName);
        return method.getAnnotation(CreationPolicy.class).value();
    }

    private static class Handlers {
        // いずれかひとつをコマンドハンドラのメソッドに付与する
        @CreationPolicy(AggregateCreationPolicy.ALWAYS)  // 常に新規作成
        String always() {
            return "always";
        }

        @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)  // 存在しない場合のみ作成
        String createIfMissing() {
            return "createIfMissing";
        }

        @CreationPolicy(AggregateCreationPolicy.NEVER)  // 作成しない（既存のAggregateのみ対象）
        String never() {
            return "never";
        }
    }
}
