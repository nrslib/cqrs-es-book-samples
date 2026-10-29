// 検証補助: 3.43
package org.example.cqrsesbooksamples.chapter03._59;

import java.util.concurrent.atomic.AtomicInteger;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class Chapter03_59TestApplication {

    record PoisonEvent(String id) {}

    /** リスト3.42のエラーハンドラ設定を適用させるための、必ず失敗するテスト用ハンドラ */
    @Component
    @ProcessingGroup("user-projection")
    static class FailingProjectionStub {
        static final AtomicInteger attempts = new AtomicInteger();

        @EventHandler
        public void on(PoisonEvent event) {
            attempts.incrementAndGet();
            throw new IllegalStateException("処理に失敗しました: " + event.id());
        }
    }
}
