// 紙面照合: 6.27
package org.example.cqrsesbooksamples.chapter06._34;

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
    private List<String> itemIds;

    protected TodoListAggregate() {}

    @CommandHandler
    public TodoListAggregate(RegisterTodoListCommand command) throws Exception {
        var todoListId = UUID.randomUUID().toString();
        AggregateLifecycle.apply(new TodoListCreatedEvent(todoListId, command.date()));
        for (var text : command.itemTexts()) {
            var itemId = UUID.randomUUID().toString();
            AggregateLifecycle.apply(new TodoListItemAddedEvent(todoListId, itemId, text));
            // 同じUnit of Workの中でやること集約を直接生成する
            AggregateLifecycle.createNew(TodoItemAggregate.class,
                () -> new TodoItemAggregate(itemId, todoListId, text));
        }
    }

    @EventSourcingHandler
    public void on(TodoListCreatedEvent event) {
        this.todoListId = event.todoListId();
        this.itemIds = new ArrayList<>();
    }

    @EventSourcingHandler
    public void on(TodoListItemAddedEvent event) {
        this.itemIds.add(event.itemId());
    }
}
