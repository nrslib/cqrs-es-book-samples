package org.example.cqrsesbooksamples.chapter02._7;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.Test;

class UserAggregateTest {
    // 削除済み（旧 2.7）
    @Test
    void コンストラクタコマンドハンドラが不正なメールアドレスを拒否する() {
        var fixture = new AggregateTestFixture<>(UserAggregate.class);

        fixture.givenNoPriorActivity()
            .when(new CreateUserCommand("John", ""))
            .expectException(IllegalArgumentException.class);
    }
}
