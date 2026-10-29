package org.example.cqrsesbooksamples.chapter03._38;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("event-handler-group")
public class EventHandlerGroupComponent {
    @EventHandler
    public void on(SomethingHappenedEvent event) {
        // PooledStreamingEventProcessor登録のデモ用ハンドラ
    }
}
