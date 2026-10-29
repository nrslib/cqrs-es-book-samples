package org.example.cqrsesbooksamples.chapter06._26;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

// 削除済み（旧 6.29）
@Component
public record TodoItemCreationHandler(CommandGateway commandGateway) {
    @EventHandler
    public void on(TodoItemCreationRequestedEvent event) {
        commandGateway.send(
            new CreateTodoItemCommand(event.itemId(), event.todoListId(), event.text()));
    }
}
