// 紙面照合: 2.29
package org.example.cqrsesbooksamples.chapter02._37;

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
    private String email;

    public UserAggregate() {
    }

    @CommandHandler
    public UserAggregate(CreateUserCommand command) {
        var userId = UUID.randomUUID().toString();
        AggregateLifecycle.apply(new UserCreatedEvent(userId, command.name(), command.email()));
    }


    @CommandHandler
    public void handle(UpdateUserCommand command) {
        // 名前が変更された場合のみ名前更新イベントを発行
        if (!this.name.equals(command.name())) {
            AggregateLifecycle.apply(new UserNameUpdatedEvent(
                command.userId(),
                command.name()
            ));
        }

        // メールアドレスが変更された場合のみメール更新イベントを発行
        if (!this.email.equals(command.email())) {
            AggregateLifecycle.apply(new UserEmailUpdatedEvent(
                command.userId(),
                command.email()
            ));
        }
    }

    @EventSourcingHandler
    public void on(UserCreatedEvent event) {
        this.userId = event.userId();
        this.name = event.name();
        this.email = event.email();
    }

    @EventSourcingHandler
    public void on(UserNameUpdatedEvent event) {
        this.name = event.name();
    }

    @EventSourcingHandler
    public void on(UserEmailUpdatedEvent event) {
        this.email = event.email();
    }
}
