package org.example.cqrsesbooksamples.chapter02._63;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("user-projection")
public class UserProjection {
    private final Set<String> handledUserIds = ConcurrentHashMap.newKeySet();

    @EventHandler
    public void on(UserRegisteredEvent event) {
        handledUserIds.add(event.userId());
    }

    public Set<String> handledUserIds() {
        return handledUserIds;
    }
}
