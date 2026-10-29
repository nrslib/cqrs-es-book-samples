// 検証補助: 2.28
package org.example.cqrsesbooksamples.chapter02._36;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void 更新メソッドは更新イベントを返す() {
        var user = new User("user-123");

        var event = user.update("John Doe", "john.doe@example.com");

        assertThat(event).isEqualTo(
            new UserUpdatedEvent("user-123", "John Doe", "john.doe@example.com"));
    }

    @Test
    void 作成イベントを適用するとユーザが復元される() {
        var event = User.create("user-123", "John", "john@example.com");

        var user = User.applyEvent(event);

        assertThat(user.userId()).isEqualTo("user-123");
    }
}
