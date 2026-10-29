// 紙面照合: 2.9
package org.example.cqrsesbooksamples.chapter02._2;


public record UserCreatedEvent(
    String userId,  // 生成されたユーザID
    String name,    // ユーザ名
    String email    // メールアドレス
) implements UserEvent {
}
