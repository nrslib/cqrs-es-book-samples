package org.example.cqrsesbooksamples.chapter02._45;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

// 削除済み（旧 2.45）
@Aggregate
public class UserAggregateAdapter {
    private User user;  // ドメインオブジェクト

    @EventSourcingHandler
    public void on(UserEvent event) {
        if (user != null) {
            // 既存のユーザに対するイベント適用
            user = user.applyEvent(event);
        } else {
            // 新規ユーザ作成の場合
            if (event instanceof UserCreatedEvent userCreated) {
                user = User.applyEvent(userCreated);
            } else {
                throw new IllegalArgumentException("Event is not a UserCreatedEvent");
            }
        }
    }
}
