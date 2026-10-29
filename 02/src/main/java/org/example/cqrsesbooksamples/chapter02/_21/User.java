// 紙面照合: 2.16
package org.example.cqrsesbooksamples.chapter02._21;


public record User(String userId) {
    // ユーザ作成のファクトリメソッド
    public static UserCreatedEvent create(String userId, String name, String email) {
        // バリデーション
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        // イベントを生成して返却
        return new UserCreatedEvent(userId, name, email);
    }

    // イベントの適用（静的メソッド - 新規作成用）
    public static User applyEvent(UserCreatedEvent event) {
        return new User(event.userId());
    }

    // イベントの適用（インスタンスメソッド - 更新用）
    public User applyEvent(UserEvent event) {
        return switch (event) {
            case UserCreatedEvent e -> applyEvent(e);
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }
}
