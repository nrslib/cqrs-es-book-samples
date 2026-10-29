package org.example.cqrsesbooksamples.chapter03._06;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class Chapter03_06TestApplication {

    /** 旧 3.6のプロセッサ設定を適用させるためのテスト用ハンドラ */
    @Component
    @ProcessingGroup("user-projection-v2")
    static class UserProjectionV2Stub {
        @EventHandler
        public void on(String event) {
            // 何もしない
        }
    }
}
