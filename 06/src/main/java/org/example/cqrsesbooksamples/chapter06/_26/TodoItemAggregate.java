package org.example.cqrsesbooksamples.chapter06._26;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

// 書籍リスト外の補助実装（作成要求の宛先となる、やること集約）
@Aggregate
public class TodoItemAggregate {

    @AggregateIdentifier
    private String itemId;

    protected TodoItemAggregate() {}

    @CommandHandler
    public TodoItemAggregate(CreateTodoItemCommand command) {
        AggregateLifecycle.apply(
            new TodoItemCreatedEvent(command.itemId(), command.todoListId(), command.text()));
    }

    @EventSourcingHandler
    public void on(TodoItemCreatedEvent event) {
        this.itemId = event.itemId();
    }
}
