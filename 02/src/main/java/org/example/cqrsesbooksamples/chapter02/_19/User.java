// 紙面照合: 2.38, 2.41
package org.example.cqrsesbooksamples.chapter02._19;

public record User(String userId, boolean deleted) {
    // deletedはここに定義されるのでAggregateAdapterには不要になる
    // ユーザ作成のファクトリメソッド
    public static UserCreatedEvent create(String userId, String name, String email) {
        // バリデーション
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        // イベントを生成して返却
        return new UserCreatedEvent(userId, name, email);
    }

    // ユーザ更新メソッド

    public UserUpdatedEvent update(String name, String email) {
        // 削除済みのユーザは更新できない
        if (deleted) {
            throw new IllegalStateException("Cannot update deleted user");
        }

        // 更新イベントを生成して返却
        return new UserUpdatedEvent(userId, name, email);
    }

    // ユーザ削除メソッド
    public UserDeletedEvent delete() {
        return new UserDeletedEvent(userId);
    }

    // イベントの適用（静的メソッド - 新規作成用）
    public static User applyEvent(UserCreatedEvent event) {
        return new User(event.userId(), false);
    }

    // イベント適用（インスタンスメソッド）

    public User applyEvent(UserEvent event) {
        return switch (event) {
            case UserCreatedEvent userCreatedEvent -> applyEvent(userCreatedEvent);
            case UserUpdatedEvent _ -> this; // 現状、更新イベントのデータは活用していないのでそのまま返却する
            case UserDeletedEvent _ -> new User(userId, true);  // 削除イベントの場合は削除フラグをtrueにした新しいインスタンスを返す
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }
}
