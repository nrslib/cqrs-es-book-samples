// 紙面照合: 2.14, 2.15, 2.17, 2.28
package org.example.cqrsesbooksamples.chapter02._19;

import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;


@Aggregate
public class UserAggregateAdapter {
    @AggregateIdentifier
    public String userId() {
        // ドメインオブジェクトから識別子を取得
        if (user == null) {
            return null;
        }
        return user.userId();
    }

    private User user;  // 集約の本体（ドメインオブジェクト）

    public UserAggregateAdapter() {
    }


    @CommandHandler
    public UserAggregateAdapter(CreateUserCommand command) {
        // 新しいユーザIDを生成
        var userId = UUID.randomUUID().toString();

        // ドメインオブジェクトの静的メソッドを呼び出してイベントを生成
        var event = User.create(userId, command.name(), command.email());

        // イベントを適用
        AggregateLifecycle.apply(event);
    }


    @CommandHandler
    public void handle(UpdateUserCommand command) {
        // ドメインオブジェクトのメソッドを呼び出してイベントを生成
        var event = user.update(
            command.name(),
            command.email()
        );
        // イベントを適用
        AggregateLifecycle.apply(event);
    }

    @CommandHandler
    public void handle(DeleteUserCommand command) {
        AggregateLifecycle.apply(user.delete());
    }

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
