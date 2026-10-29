// 検証補助: 3.11
package org.example.cqrsesbooksamples.chapter03._43;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class Chapter03_43TestApplication {

    /** リスト3.11のプロセッサ設定を適用させるためのテスト用ハンドラ */
    @Component
    @ProcessingGroup("user-projection")
    static class UserProjectionStub {
        @EventHandler
        public void on(String event) {
            // 何もしない
        }
    }
}
