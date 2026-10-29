// 紙面照合: 6.25
package org.example.cqrsesbooksamples.chapter06._30;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;


@Component
public record TodoItemMaterializationHandler(CommandGateway commandGateway) {
    @EventHandler
    public void on(TodoListItemAddedEvent event) {
        commandGateway.send(
            new CreateTodoItemCommand(event.itemId(), event.todoListId(), event.text()));
    }
}
