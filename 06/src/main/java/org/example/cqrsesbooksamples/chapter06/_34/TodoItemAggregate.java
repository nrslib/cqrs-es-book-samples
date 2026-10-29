// 紙面照合: 6.27
package org.example.cqrsesbooksamples.chapter06._34;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;


@Aggregate
public class TodoItemAggregate {

    @AggregateIdentifier
    private String itemId;

    protected TodoItemAggregate() {}

    // createNewのファクトリから呼ばれ、自分の生成イベントを自分で積む
    public TodoItemAggregate(String itemId, String todoListId, String text) {
        AggregateLifecycle.apply(new TodoItemCreatedEvent(itemId, todoListId, text));
    }

    @EventSourcingHandler
    public void on(TodoItemCreatedEvent event) {
        this.itemId = event.itemId();
    }
}
