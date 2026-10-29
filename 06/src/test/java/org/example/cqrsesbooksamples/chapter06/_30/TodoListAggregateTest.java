package org.example.cqrsesbooksamples.chapter06._30;

import static org.axonframework.test.matchers.Matchers.andNoMore;
import static org.axonframework.test.matchers.Matchers.exactSequenceOf;
import static org.axonframework.test.matchers.Matchers.matches;
import static org.axonframework.test.matchers.Matchers.payloadsMatching;

import java.time.LocalDate;
import java.util.List;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.Test;

class TodoListAggregateTest {

    private final AggregateTestFixture<TodoListAggregate> fixture =
        new AggregateTestFixture<>(TodoListAggregate.class);

    @Test
    void 一括登録は生成イベントと事実イベントだけを積む() {
        fixture.givenNoPriorActivity()
            .when(new RegisterTodoListCommand(
                LocalDate.of(2026, 7, 8), List.of("資料を書く", "レビューを返す")))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(payloadsMatching(exactSequenceOf(
                matches(p -> p instanceof TodoListCreatedEvent),
                matches(p -> p instanceof TodoListItemAddedEvent e
                    && e.text().equals("資料を書く")),
                matches(p -> p instanceof TodoListItemAddedEvent e
                    && e.text().equals("レビューを返す")),
                andNoMore())));
    }

    @Test
    void 日中の追加も同じ事実イベントになる() {
        fixture.given(new TodoListCreatedEvent("list-1", LocalDate.of(2026, 7, 8)))
            .when(new AddTodoItemCommand("list-1", "夜のふりかえり"))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(payloadsMatching(exactSequenceOf(
                matches(p -> p instanceof TodoListItemAddedEvent e
                    && e.text().equals("夜のふりかえり")),
                andNoMore())));
    }

    @Test
    void 締めたリストには追加できない() {
        fixture.given(
                new TodoListCreatedEvent("list-1", LocalDate.of(2026, 7, 8)),
                new TodoListClosedEvent("list-1"))
            .when(new AddTodoItemCommand("list-1", "夜のふりかえり"))
            .expectException(IllegalStateException.class);
    }
}
