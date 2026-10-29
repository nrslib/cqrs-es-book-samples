// 紙面照合: 3.37
package org.example.cqrsesbooksamples.chapter03._33;

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
    public UserAggregate(RegisterUserCommand cmd) {
        // イベントには個人情報の参照IDのみを含める
        var event = new UserRegisteredEvent(
            cmd.userId(),
            cmd.personalInfoId(),
            cmd.displayName()
        );
        AggregateLifecycle.apply(event);
    }

    @CommandHandler
    public void handle(DeleteUserCommand cmd) {
        AggregateLifecycle.apply(new UserDeletedEvent(cmd.userId()));
    }

    @EventSourcingHandler
    public void on(UserRegisteredEvent event) {
        this.userId = event.userId();
    }
}
