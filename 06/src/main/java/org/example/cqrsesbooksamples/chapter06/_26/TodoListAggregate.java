// 紙面照合: 6.23
package org.example.cqrsesbooksamples.chapter06._26;

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

    @CommandHandler
    public TodoListAggregate(RegisterTodoListCommand command) {
        var todoListId = UUID.randomUUID().toString();
        AggregateLifecycle.apply(new TodoListCreatedEvent(todoListId, command.date()));
        for (var text : command.itemTexts()) {
            var itemId = UUID.randomUUID().toString();
            AggregateLifecycle.apply(new TodoListItemLinkedEvent(todoListId, itemId));
            AggregateLifecycle.apply(new TodoItemCreationRequestedEvent(todoListId, itemId, text));
        }
    }

    @CommandHandler
    public void handle(AddTodoItemCommand command) {
        if (status == TodoListStatus.CLOSED) {
            throw new IllegalStateException("締めたリストにはやることを追加できません");
        }
        var itemId = UUID.randomUUID().toString();
        AggregateLifecycle.apply(new TodoListItemLinkedEvent(todoListId, itemId));
        AggregateLifecycle.apply(new TodoItemCreationRequestedEvent(todoListId, itemId, command.text()));
    }

    @EventSourcingHandler
    public void on(TodoListCreatedEvent event) {
        this.todoListId = event.todoListId();
        this.status = TodoListStatus.OPEN;
        this.itemIds = new ArrayList<>();
    }

    @EventSourcingHandler
    public void on(TodoListItemLinkedEvent event) {
        this.itemIds.add(event.itemId());
    }

    // 書籍では省略（リストを締める操作）
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
