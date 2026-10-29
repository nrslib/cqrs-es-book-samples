package org.example.cqrsesbooksamples.chapter02._17;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class UserAggregate {
    @AggregateIdentifier
    private String userId;

    public UserAggregate() {
    }

    @CommandHandler
    public UserAggregate(CreateUserCommand command) {
        apply(new UserCreatedEvent(UUID.randomUUID().toString(), command.name(), command.email()));
    }

    @CommandHandler
    public void handle(UpdateUserCommand command) {
        apply(new UserUpdatedEvent(command.userId(), command.name(), command.email()));
    }

    @EventSourcingHandler
    public void on(UserCreatedEvent event) {
        this.userId = event.userId();
    }
}
