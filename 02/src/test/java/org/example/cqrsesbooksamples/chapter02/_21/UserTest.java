// 検証補助: 2.16
package org.example.cqrsesbooksamples.chapter02._21;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void 作成イベントからユーザーIDを復元できる() {
        var event = new UserCreatedEvent("user-123", "John", "john@example.com");

        var user = User.applyEvent(event);

        assertThat(user.userId()).isEqualTo("user-123");
    }
}
