package org.example.cqrsesbooksamples.chapter02._13;

import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class UserAggregate {
    @AggregateIdentifier
    private String userId;

    public UserAggregate() {
    }

    @CommandHandler
    public UserAggregate(CreateUserCommand command) {
        var userId = UUID.randomUUID().toString();
        AggregateLifecycle.apply(new UserCreatedEvent(userId, command.name(), command.email()));
    }

    // 削除済み（旧 2.13）
    @EventSourcingHandler
    public void on(UserEvent event) {
        if (event instanceof UserCreatedEvent userCreatedEvent) {
            // UserCreatedEventの場合の処理
            this.userId = userCreatedEvent.userId();
        } else {
            // 未知のイベント型の場合
            throw new IllegalArgumentException("Unknown event type");
        }
    }
}
