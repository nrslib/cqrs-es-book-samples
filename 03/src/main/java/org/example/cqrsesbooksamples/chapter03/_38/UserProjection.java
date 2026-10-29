package org.example.cqrsesbooksamples.chapter03._38;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("user-projection")
public class UserProjection {
    @EventHandler
    public void on(SomethingHappenedEvent event) {
        // 処理グループ定義のデモ用ハンドラ
    }
}
