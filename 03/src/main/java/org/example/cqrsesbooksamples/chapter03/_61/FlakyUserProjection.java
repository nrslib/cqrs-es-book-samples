package org.example.cqrsesbooksamples.chapter03._61;

import java.util.concurrent.atomic.AtomicBoolean;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

/**
 * 失敗を再現できるプロジェクション。
 * failing が true のあいだは処理が失敗し、イベントはデッドレターキューに入る。
 */
@Component
@ProcessingGroup("user-projection")
public class FlakyUserProjection {
    private final UserReadModelRepository repository;
    private final AtomicBoolean failing = new AtomicBoolean(false);

    public FlakyUserProjection(UserReadModelRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(UserRegisteredEvent event) {
        if (failing.get()) {
            throw new IllegalStateException("プロジェクションの不具合: " + event.userId());
        }
        repository.save(new UserReadModel(event.userId(), event.name()));
    }

    public void setFailing(boolean value) {
        failing.set(value);
    }
}
