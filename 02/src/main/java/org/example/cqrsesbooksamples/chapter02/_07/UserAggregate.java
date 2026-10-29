package org.example.cqrsesbooksamples.chapter02._7;

import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

// 削除済み（旧 2.7）
@Aggregate
public class UserAggregate {
    @AggregateIdentifier
    private String userId;  // 集約を一意に識別するID

    public UserAggregate() {
        // イベントソーシングでの再構築時に使用されるデフォルトコンストラクタ
    }

    @CommandHandler
    public UserAggregate(CreateUserCommand command) {
        // バリデーションやビジネスロジックの実行
        if (command.email() == null || command.email().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        // 新しいユーザIDを生成
        var userId = UUID.randomUUID().toString();

        // ユーザ作成イベントを生成
        var event = new UserCreatedEvent(
            userId,
            command.name(),
            command.email()
        );

        // イベントを適用して状態を変更
        AggregateLifecycle.apply(event);
    }
}
