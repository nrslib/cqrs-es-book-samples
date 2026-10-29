// 検証補助: 2.31
package org.example.cqrsesbooksamples.chapter02._39;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void 更新イベントを適用しても再構築済みの状態を維持する() {
        var user = new User("user-123");
        UserEvent event = new UserUpdatedEvent("user-123", "John Doe", "john.doe@example.com");

        var updated = user.applyEvent(event);

        assertThat(updated).isSameAs(user);
    }
}
