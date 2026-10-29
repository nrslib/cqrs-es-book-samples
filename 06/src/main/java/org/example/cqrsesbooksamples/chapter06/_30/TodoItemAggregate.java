// 紙面照合: 6.26
package org.example.cqrsesbooksamples.chapter06._30;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.CreationPolicy;
import org.axonframework.spring.stereotype.Aggregate;


@Aggregate
public class TodoItemAggregate {

    @AggregateIdentifier
    private String itemId;
    private boolean created;

    protected TodoItemAggregate() {}

    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    public void handle(CreateTodoItemCommand command) {
        if (created) {
            return;
        }
        AggregateLifecycle.apply(
            new TodoItemCreatedEvent(command.itemId(), command.todoListId(), command.text()));
    }

    @EventSourcingHandler
    public void on(TodoItemCreatedEvent event) {
        this.itemId = event.itemId();
        this.created = true;
    }
}
