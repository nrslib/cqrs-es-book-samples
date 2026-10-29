// 検証補助: 2.14, 2.15, 2.17, 2.28, 2.38, 2.41
package org.example.cqrsesbooksamples.chapter02._19;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.matchers.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserAggregateAdapterTest {
    private AggregateTestFixture<UserAggregateAdapter> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(UserAggregateAdapter.class);
    }


    @Test
    void ドメインオブジェクトがイベントを組み立てて作成できる() {
        fixture.givenNoPriorActivity()
            .when(new CreateUserCommand("John", "john@example.com"))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(Matchers.exactSequenceOf(
                Matchers.messageWithPayload(Matchers.predicate(
                    (UserCreatedEvent event) ->
                        event.userId() != null && event.name().equals("John")))));
    }

    @Test
    void メールアドレスが空ならドメインのバリデーションで弾かれる() {
        fixture.givenNoPriorActivity()
            .when(new CreateUserCommand("John", ""))
            .expectException(IllegalArgumentException.class);
    }

    // 削除済み（旧 2.33）
    @Test
    void 更新はドメインオブジェクトに委譲される() {
        fixture.given(new UserCreatedEvent("user-123", "John", "john@example.com"))
            .when(new UpdateUserCommand("user-123", "John Doe", "john.doe@example.com"))
            .expectEvents(new UserUpdatedEvent("user-123", "John Doe", "john.doe@example.com"));
    }


    @Test
    void 削除済みユーザーの更新はドメインの判断で拒否される() {
        fixture.given(
                new UserCreatedEvent("user-123", "John", "john@example.com"),
                new UserDeletedEvent("user-123"))
            .when(new UpdateUserCommand("user-123", "John Doe", "john.doe@example.com"))
            .expectException(IllegalStateException.class);
    }
}
