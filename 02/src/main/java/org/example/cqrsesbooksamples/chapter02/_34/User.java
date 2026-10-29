package org.example.cqrsesbooksamples.chapter02._34;

// 削除済み（旧 2.34）
public record User(String userId) {
    public static UserCreatedEvent create(String userId, String name, String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        return new UserCreatedEvent(userId, name, email);
    }

    public static User applyEvent(UserCreatedEvent event) {
        return new User(event.userId());
    }

    // イベントの適用（インスタンスメソッド）
    public User applyEvent(UserEvent event) {
        return switch (event) {
            case UserCreatedEvent userCreatedEvent -> applyEvent(userCreatedEvent);
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }
}
