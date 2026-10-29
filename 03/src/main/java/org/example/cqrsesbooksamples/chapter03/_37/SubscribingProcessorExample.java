package org.example.cqrsesbooksamples.chapter03._37;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.modelling.command.AggregateLifecycle;

public class SubscribingProcessorExample {
    // 削除済み（旧 3.53）
    @CommandHandler
    public void handle(SomeCommand command) {
        // 集約内でイベントを発行
        AggregateLifecycle.apply(new SomeEvent());
        // SubscribingEventProcessorによるハンドラも同じトランザクション内で実行される
        // どこかで例外が発生するとすべてロールバック
    }
}
