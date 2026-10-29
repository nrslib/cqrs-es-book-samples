// 検証補助: 2.29
package org.example.cqrsesbooksamples.chapter02._37;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserAggregateTest {
    private AggregateTestFixture<UserAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(UserAggregate.class);
    }


    @Test
    void 名前だけ変更すると名前更新イベントのみ発行される() {
        fixture.given(new UserCreatedEvent("user-123", "John", "john@example.com"))
            .when(new UpdateUserCommand("user-123", "John Doe", "john@example.com"))
            .expectEvents(new UserNameUpdatedEvent("user-123", "John Doe"));
    }

    @Test
    void 名前とメールを変更すると2つのイベントが発行される() {
        fixture.given(new UserCreatedEvent("user-123", "John", "john@example.com"))
            .when(new UpdateUserCommand("user-123", "John Doe", "john.doe@example.com"))
            .expectEvents(
                new UserNameUpdatedEvent("user-123", "John Doe"),
                new UserEmailUpdatedEvent("user-123", "john.doe@example.com"));
    }

    @Test
    void 変更がなければイベントは発行されない() {
        fixture.given(new UserCreatedEvent("user-123", "John", "john@example.com"))
            .when(new UpdateUserCommand("user-123", "John", "john@example.com"))
            .expectNoEvents();
    }
}
