package org.example.cqrsesbooksamples.chapter06._30;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Test;

class TodoItemMaterializationHandlerTest {

    private final CommandGateway commandGateway = mock(CommandGateway.class);
    private final TodoItemMaterializationHandler handler =
        new TodoItemMaterializationHandler(commandGateway);

    @Test
    void 追加の事実を受けたら実体化コマンドを送る() {
        handler.on(new TodoListItemAddedEvent("list-1", "item-1", "資料を書く"));

        verify(commandGateway).send(new CreateTodoItemCommand("item-1", "list-1", "資料を書く"));
    }
}
