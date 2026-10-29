package org.example.cqrsesbooksamples.chapter06._26;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Test;

class TodoItemCreationHandlerTest {

    private final CommandGateway commandGateway = mock(CommandGateway.class);
    private final TodoItemCreationHandler handler = new TodoItemCreationHandler(commandGateway);

    @Test
    void 作成要求を受けたら作成コマンドへ中継する() {
        handler.on(new TodoItemCreationRequestedEvent("list-1", "item-1", "資料を書く"));

        verify(commandGateway).send(new CreateTodoItemCommand("item-1", "list-1", "資料を書く"));
    }
}
