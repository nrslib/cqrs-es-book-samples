package org.example.cqrsesbooksamples.chapter03._37;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.Test;

/**
 * SubscribingEventProcessorと同一トランザクションで動くコマンド処理（旧 3.53）の検証。
 * 集約のコマンドハンドラから呼び出すと AggregateLifecycle.apply が実行され、
 * イベントが発行されることを確かめる。
 */
class SubscribingProcessorExampleTest {

    record ExecuteExampleCommand(String id) {}

    @SuppressWarnings("unused")
    public static class ExampleAggregate {
        @AggregateIdentifier
        private String id;

        public ExampleAggregate() {
        }

        @CommandHandler
        public ExampleAggregate(ExecuteExampleCommand command) {
            new SubscribingProcessorExample().handle(new SomeCommand());
        }

        @EventSourcingHandler
        public void on(SomeEvent event) {
            // SomeEventは識別子を持たないため、テスト用に固定値を設定する
            this.id = "example-1";
        }
    }

    @Test
    void コマンド処理の中でイベントが発行される() {
        var fixture = new AggregateTestFixture<>(ExampleAggregate.class);

        fixture.givenNoPriorActivity()
                .when(new ExecuteExampleCommand("example-1"))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new SomeEvent());
    }
}
