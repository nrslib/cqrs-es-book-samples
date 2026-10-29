// 検証補助: 2.10, 2.11
package org.example.cqrsesbooksamples.chapter02._14;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.matchers.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserAggregateTest {
    private AggregateTestFixture<UserAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(UserAggregate.class);
    }


    @Test
    void sealedイベントをパターンマッチで適用して作成できる() {
        fixture.givenNoPriorActivity()
            .when(new CreateUserCommand("John", "john@example.com"))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(Matchers.exactSequenceOf(
                Matchers.messageWithPayload(Matchers.predicate(
                    (UserCreatedEvent event) -> event.name().equals("John")))));
    }

    @Test
    void 更新イベントと削除イベントもパターンマッチで適用できる() {
        fixture.given(new UserCreatedEvent("user-123", "John", "john@example.com"))
            .when(new UpdateUserCommand("user-123", "John Doe", "john.doe@example.com"))
            .expectEvents(new UserUpdatedEvent("user-123", "John Doe", "john.doe@example.com"));

        fixture = new AggregateTestFixture<>(UserAggregate.class);
        fixture.given(new UserCreatedEvent("user-123", "John", "john@example.com"), new UserUpdatedEvent("user-123", "John Doe", "john.doe@example.com"))
            .when(new DeleteUserCommand("user-123"))
            .expectEvents(new UserDeletedEvent("user-123"));
    }
}
