package org.example.cqrsesbooksamples.chapter02._13;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.matchers.Matchers;
import org.junit.jupiter.api.Test;

class UserAggregateTest {
    // 削除済み（旧 2.13）
    @Test
    void インターフェースを受け取るイベントソーシングハンドラで状態を再構築できる() {
        var fixture = new AggregateTestFixture<>(UserAggregate.class);
        fixture.givenNoPriorActivity()
            .when(new CreateUserCommand("John", "john@example.com"))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(Matchers.exactSequenceOf(
                Matchers.messageWithPayload(Matchers.predicate(
                    (UserCreatedEvent event) -> event.userId() != null))));
    }
}
