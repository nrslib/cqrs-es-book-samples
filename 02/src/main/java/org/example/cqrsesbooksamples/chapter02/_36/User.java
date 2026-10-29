// 紙面照合: 2.28
package org.example.cqrsesbooksamples.chapter02._36;


public record User(String userId) {
    // ユーザ作成のファクトリメソッド
    public static UserCreatedEvent create(String userId, String name, String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        return new UserCreatedEvent(userId, name, email);
    }

    public static User applyEvent(UserCreatedEvent event) {
        return new User(event.userId());
    }

    // ユーザ更新メソッド
    public UserUpdatedEvent update(String name, String email) {
        return new UserUpdatedEvent(userId, name, email);
    }
}
