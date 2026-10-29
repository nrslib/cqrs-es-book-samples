package org.example.cqrsesbooksamples.chapter06._34;

import static org.axonframework.test.matchers.Matchers.listWithAllOf;
import static org.axonframework.test.matchers.Matchers.matches;
import static org.axonframework.test.matchers.Matchers.payloadsMatching;

import java.time.LocalDate;
import java.util.List;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.Test;

class TodoListAggregateTest {

    private final AggregateTestFixture<TodoListAggregate> fixture =
        new AggregateTestFixture<>(TodoListAggregate.class);

    // リストの2イベントに加えて、createNewで生成されたやること集約の生成イベントも同じUnit of Workで発行される
    @Test
    void 一括登録でリストのイベントとともにやること集約が生成される() {
        fixture.givenNoPriorActivity()
            .when(new RegisterTodoListCommand(
                LocalDate.of(2026, 7, 8), List.of("資料を書く")))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(payloadsMatching(listWithAllOf(
                matches(p -> p instanceof TodoListCreatedEvent),
                matches(p -> p instanceof TodoListItemAddedEvent e
                    && e.text().equals("資料を書く")),
                matches(p -> p instanceof TodoItemCreatedEvent e
                    && e.text().equals("資料を書く")))));
    }
}
