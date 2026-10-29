// 紙面照合: 2.7
package org.example.cqrsesbooksamples.chapter02._9;

import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.CreationPolicy;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class UserAggregate {
    @AggregateIdentifier
    private String userId;

    public UserAggregate() {
    }


    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    public CreateUserResult handle(CreateUserCommand command) {
        // 新しいユーザIDを生成
        String userId = UUID.randomUUID().toString();

        // ユーザ作成イベントを生成
        var event = new UserCreatedEvent(
            userId,
            command.name(),
            command.email()
        );

        // イベントを適用して状態を変更
        AggregateLifecycle.apply(event);

        // カスタム結果オブジェクトを返却
        return new CreateUserResult(userId, event.name(), event.email());
    }

    @EventSourcingHandler
    public void on(UserCreatedEvent event) {
        this.userId = event.userId();
    }
}
