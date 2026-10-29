// 検証補助: 3.40
package org.example.cqrsesbooksamples.chapter03._35.serializer;

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

    protected UserAggregate() {
    }

    @CommandHandler
    public UserAggregate(RegisterUserCommand command) {
        AggregateLifecycle.apply(new UserRegisteredEvent(command.userId(), command.name(), command.email()));
    }

    @EventSourcingHandler
    public void on(UserRegisteredEvent event) {
        userId = event.userId();
        name = event.name();
        email = event.email();
    }

    public UserState state() {
        return new UserState(userId, name, email);
    }

    public record UserState(String userId, String name, String email) {
    }

    public record RegisterUserCommand(String userId, String name, String email) {
    }
}
