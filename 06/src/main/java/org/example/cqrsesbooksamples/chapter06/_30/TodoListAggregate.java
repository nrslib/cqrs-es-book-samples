package org.example.cqrsesbooksamples.chapter06._30;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class TodoListAggregate {

    @AggregateIdentifier
    private String todoListId;
    private TodoListStatus status;
    private List<String> itemIds;

    protected TodoListAggregate() {}

    // 削除済み（旧 6.31）
    @CommandHandler
    public TodoListAggregate(RegisterTodoListCommand command) {
        var todoListId = UUID.randomUUID().toString();
        AggregateLifecycle.apply(new TodoListCreatedEvent(todoListId, command.date()));
        for (var text : command.itemTexts()) {
            AggregateLifecycle.apply(
                new TodoListItemAddedEvent(todoListId, UUID.randomUUID().toString(), text));
        }
    }

    // 削除済み（旧 6.31）
    @CommandHandler
    public void handle(AddTodoItemCommand command) {
        if (status == TodoListStatus.CLOSED) {
            throw new IllegalStateException("締めたリストにはやることを追加できません");
        }
        AggregateLifecycle.apply(
            new TodoListItemAddedEvent(todoListId, UUID.randomUUID().toString(), command.text()));
    }

    @EventSourcingHandler
    public void on(TodoListCreatedEvent event) {
        this.todoListId = event.todoListId();
        this.status = TodoListStatus.OPEN;
        this.itemIds = new ArrayList<>();
    }

    // 削除済み（旧 6.31）
    @EventSourcingHandler
    public void on(TodoListItemAddedEvent event) {
        this.itemIds.add(event.itemId());
    }

    @CommandHandler
    public void handle(CloseTodoListCommand command) {
        if (status == TodoListStatus.CLOSED) {
            return;
        }
        AggregateLifecycle.apply(new TodoListClosedEvent(todoListId));
    }

    @EventSourcingHandler
    public void on(TodoListClosedEvent event) {
        this.status = TodoListStatus.CLOSED;
    }
}
