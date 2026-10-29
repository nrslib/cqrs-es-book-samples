package org.example.cqrsesbooksamples.chapter02._45;

// 削除済み（旧 2.45）
// 削除済みかどうかを判定するためにdeletedフィールドを追加
public record User(String userId, boolean deleted) {
    public static UserCreatedEvent create(String userId, String name, String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        return new UserCreatedEvent(userId, name, email);
    }

    public UserUpdatedEvent update(String name, String email) {
        return new UserUpdatedEvent(userId, name, email);
    }

    // 新規ユーザ作成時のイベント適用（静的メソッド）
    public static User applyEvent(UserCreatedEvent event) {
        return new User(event.userId(), false);
    }

    // イベント適用（インスタンスメソッド）
    public User applyEvent(UserEvent event) {
        return switch (event) {
            case UserCreatedEvent userCreatedEvent -> applyEvent(userCreatedEvent);
            case UserUpdatedEvent _ -> this;  // 更新イベントの場合は現在のインスタンスをそのまま返す
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }
}
