package org.example.cqrsesbooksamples.chapter03._13;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("userProjection")
public class UserProjection {
    @EventHandler
    public void on(UserRegisteredEvent event) {
        // セグメント分割のデモ用ハンドラ
    }
}
