// 紙面照合: 2.31
package org.example.cqrsesbooksamples.chapter02._39;


public record User(String userId) {
    public static UserCreatedEvent create(String userId, String name, String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        return new UserCreatedEvent(userId, name, email);
    }

    public UserUpdatedEvent update(String name, String email) {
        return new UserUpdatedEvent(userId, name, email);
    }

    public static User applyEvent(UserCreatedEvent event) {
        return new User(event.userId());
    }

    public User applyEvent(UserEvent event) {
        return switch (event) {
            case UserCreatedEvent e -> applyEvent(e);
            case UserUpdatedEvent _ -> this;  // 更新イベントの場合は現在のインスタンスをそのまま返す
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }
}
