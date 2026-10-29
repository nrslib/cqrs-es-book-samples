package org.example.cqrsesbooksamples.chapter06._26;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.Test;

class TodoItemAggregateTest {

    private final AggregateTestFixture<TodoItemAggregate> fixture =
        new AggregateTestFixture<>(TodoItemAggregate.class);

    @Test
    void 作成コマンドがやること集約を生成して生成イベントを積む() {
        fixture.givenNoPriorActivity()
            .when(new CreateTodoItemCommand("item-1", "list-1", "資料を書く"))
            .expectEvents(new TodoItemCreatedEvent("item-1", "list-1", "資料を書く"));
    }
}
