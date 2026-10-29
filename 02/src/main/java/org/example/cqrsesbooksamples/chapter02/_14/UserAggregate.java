// 紙面照合: 2.11
package org.example.cqrsesbooksamples.chapter02._14;

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
    private String name;
    private boolean active;

    public UserAggregate() {
    }

    @CommandHandler
    public UserAggregate(CreateUserCommand command) {
        var userId = UUID.randomUUID().toString();
        AggregateLifecycle.apply(new UserCreatedEvent(userId, command.name(), command.email()));
    }

    @CommandHandler
    public void handle(UpdateUserCommand command) {
        AggregateLifecycle.apply(new UserUpdatedEvent(command.userId(), command.name(), command.email()));
    }

    @CommandHandler
    public void handle(DeleteUserCommand command) {
        AggregateLifecycle.apply(new UserDeletedEvent(command.userId()));
    }


    @EventSourcingHandler
    public void on(UserEvent event) {
        switch (event) {
            case UserCreatedEvent e -> {
                this.userId = e.userId();
                this.name = e.name();
                this.active = true;
            }
            case UserUpdatedEvent e -> this.name = e.name();
            case UserDeletedEvent e -> this.active = false;
            // すべての可能なイベント型を網羅していないとコンパイルエラー
        }
    }
}
