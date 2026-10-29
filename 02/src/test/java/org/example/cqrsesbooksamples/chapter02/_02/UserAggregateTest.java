// 検証補助: 2.2, 2.3, 2.9, 2.26, 2.27, 2.30, 2.35, 2.36
// 検証補助: 2.37, 2.39, 2.40
package org.example.cqrsesbooksamples.chapter02._2;

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

    // 削除済み（旧 2.12）
    @Test
    void ユーザーを作成するとUserCreatedEventが発行される() {
        fixture.givenNoPriorActivity()
            .when(new CreateUserCommand("John", "john@example.com"))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(Matchers.exactSequenceOf(
                Matchers.messageWithPayload(Matchers.predicate(
                    (UserCreatedEvent event) ->
                        event.userId() != null
                            && event.name().equals("John")
                            && event.email().equals("john@example.com")))));
    }

    @Test
    void メールアドレスが空ならユーザーを作成できない() {
        fixture.givenNoPriorActivity()
            .when(new CreateUserCommand("John", ""))
            .expectException(IllegalArgumentException.class);
    }

    // 削除済み（旧 2.32）
    @Test
    void ユーザーを更新するとUserUpdatedEventが発行される() {
        fixture.given(new UserCreatedEvent("user-123", "John", "john@example.com"))
            .when(new UpdateUserCommand("user-123", "John Doe", "john.doe@example.com"))
            .expectEvents(new UserUpdatedEvent("user-123", "John Doe", "john.doe@example.com"));
    }

    // 削除済み（旧 2.44）
    @Test
    void ユーザーを削除するとUserDeletedEventが発行される() {
        fixture.given(
                new UserCreatedEvent("user-123", "John", "john@example.com"),
                new UserUpdatedEvent("user-123", "John Doe", "john.doe@example.com"))
            .when(new DeleteUserCommand("user-123"))
            .expectEvents(new UserDeletedEvent("user-123"));
    }


    @Test
    void 削除済みユーザーは更新できない() {
        fixture.given(
                new UserCreatedEvent("user-123", "John", "john@example.com"),
                new UserDeletedEvent("user-123"))
            .when(new UpdateUserCommand("user-123", "John Doe", "john.doe@example.com"))
            .expectException(IllegalStateException.class);
    }

    @Test
    void 削除済みユーザーへの削除コマンドは何も発行しない() {
        fixture.given(
                new UserCreatedEvent("user-123", "John", "john@example.com"),
                new UserDeletedEvent("user-123"))
            .when(new DeleteUserCommand("user-123"))
            .expectSuccessfulHandlerExecution()
            .expectNoEvents();
    }
}
