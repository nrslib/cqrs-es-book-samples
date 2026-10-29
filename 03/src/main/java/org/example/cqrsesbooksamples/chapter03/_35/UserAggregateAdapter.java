// 紙面照合: 3.39
package org.example.cqrsesbooksamples.chapter03._35;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;


@Aggregate
public class UserAggregateAdapter {
    @AggregateIdentifier
    public String userId() {
        if (user == null) {
            return null;
        }
        return user.userId();
    }

    private User user;

    public UserAggregateAdapter() {
    }

    @CommandHandler
    public UserAggregateAdapter(RegisterUserCommand cmd) {
        var event = User.create(
            cmd.userId(),
            cmd.keyReference(),
            cmd.personalData()
        );
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(UserRegisteredEvent event) {
        user = User.applyEvent(event);
    }
}
