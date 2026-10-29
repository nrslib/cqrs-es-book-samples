package org.example.cqrsesbooksamples.chapter02._45;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.Test;

class UserAggregateAdapterTest {
    // 削除済み（旧 2.45）
    @Test
    void 作成イベントから削除前の状態を再構築できる() {
        var created = new UserCreatedEvent("user-123", "John", "john@example.com");

        var user = User.applyEvent(created);

        assertThat(user.userId()).isEqualTo("user-123");
        assertThat(user.deleted()).isFalse();
    }

    @Test
    void アダプタが作成イベントと更新イベントを順に適用できる() {
        var adapter = new UserAggregateAdapter();

        adapter.on(new UserCreatedEvent("user-123", "John", "john@example.com"));

        assertThatNoException().isThrownBy(() ->
            adapter.on(new UserUpdatedEvent("user-123", "John Doe", "john.doe@example.com")));
    }
}
