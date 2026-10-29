// 紙面照合: 2.2, 2.3, 2.27, 2.30, 2.36, 2.37, 2.40
package org.example.cqrsesbooksamples.chapter02._2;

import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class UserAggregate {
    @AggregateIdentifier
    private String userId;  // 集約を一意に識別するID

    private boolean deleted;  // 削除フラグ

    public UserAggregate() {
    }


    @CommandHandler
    public UserAggregate(CreateUserCommand command) {
        // バリデーションやビジネスロジックの実行
        if (command.email() == null || command.email().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        var userId = UUID.randomUUID().toString();  // 新しいユーザIDを生成

        // イベントの発行と適用
        var event = new UserCreatedEvent(
            userId,
            command.name(),
            command.email()
        );

        AggregateLifecycle.apply(event);  // イベントを適用して状態を変更
    }


    @CommandHandler
    public void handle(UpdateUserCommand command) {
        // 削除済みのユーザは更新できない
        if (deleted) {
            throw new IllegalStateException("Cannot update deleted user");
        }

        // ユーザ更新イベントを生成
        var event = new UserUpdatedEvent(
            command.userId(),
            command.name(),
            command.email()
        );

        // イベントを適用
        AggregateLifecycle.apply(event);
    }


    @CommandHandler
    public void handle(DeleteUserCommand command) {
        // すでに削除済みの場合
        if (this.deleted) {
            // 要件によって例外を投げるか、成功として扱うか決める
            return;
        }

        // 削除イベントを生成して適用
        var event = new UserDeletedEvent(command.userId());
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(UserCreatedEvent event) {
        this.userId = event.userId();  // イベントからユーザIDを取得して状態を更新
    }

    @EventSourcingHandler
    public void on(UserUpdatedEvent event) {
        // 特に何も更新する必要がない場合は空のメソッドでもOK
    }


    @EventSourcingHandler
    public void on(UserDeletedEvent event) {
        this.deleted = true;  // 削除フラグをtrueに設定
    }
}
