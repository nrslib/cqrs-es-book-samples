package org.example.cqrsesbooksamples.chapter06._30;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.Test;

class TodoItemAggregateTest {

    private final AggregateTestFixture<TodoItemAggregate> fixture =
        new AggregateTestFixture<>(TodoItemAggregate.class);

    @Test
    void 作成コマンドで実体が作られる() {
        fixture.givenNoPriorActivity()
            .when(new CreateTodoItemCommand("item-1", "list-1", "資料を書く"))
            .expectEvents(new TodoItemCreatedEvent("item-1", "list-1", "資料を書く"));
    }

    @Test
    void 同じ作成コマンドが二度届いても実体はひとつに保たれる() {
        fixture.given(new TodoItemCreatedEvent("item-1", "list-1", "資料を書く"))
            .when(new CreateTodoItemCommand("item-1", "list-1", "資料を書く"))
            .expectSuccessfulHandlerExecution()
            .expectNoEvents();
    }
}
