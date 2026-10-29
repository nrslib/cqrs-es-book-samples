package org.example.cqrsesbooksamples.chapter03._45;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("user-projection")
public class UserProjection {
    @EventHandler
    public void on(UserRegisteredEvent event) {
        // セグメント分割・マージのデモ用ハンドラ
    }
}
