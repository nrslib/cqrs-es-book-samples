package org.example.cqrsesbooksamples.chapter03._68;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("retry-projection")
public class FlakyUserProjection {
    private final AtomicInteger attempts = new AtomicInteger();
    private final List<String> processedUserIds = new CopyOnWriteArrayList<>();

    @EventHandler
    public void on(UserRegisteredEvent event) {
        // 最初の2回は障害を模して失敗させる
        if (attempts.incrementAndGet() <= 2) {
            throw new IllegalStateException("一時的な障害");
        }
        processedUserIds.add(event.userId());
    }

    public int attempts() {
        return attempts.get();
    }

    public List<String> processedUserIds() {
        return processedUserIds;
    }
}
