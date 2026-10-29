package org.example.cqrsesbooksamples.chapter03._41;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("user-projection")
public class CountingProjection {
    private final List<String> handledUserIds = new CopyOnWriteArrayList<>();

    @EventHandler
    public void on(UserRegisteredEvent event) {
        handledUserIds.add(event.userId());
    }

    public List<String> handledUserIds() {
        return List.copyOf(handledUserIds);
    }
}
