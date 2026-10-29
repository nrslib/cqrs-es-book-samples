package org.example.cqrsesbooksamples.chapter02._34;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTest {
    // 削除済み（旧 2.34）
    @Test
    void 共通イベント型から作成イベントを振り分けて適用できる() {
        UserEvent event = new UserCreatedEvent("user-123", "John", "john@example.com");

        var user = new User("before").applyEvent(event);

        assertThat(user.userId()).isEqualTo("user-123");
    }
}
