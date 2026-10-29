package org.example.cqrsesbooksamples.chapter02._72;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

// 削除済み（旧 2.72）
@Component
public class UserProjection {
    private final UserReadModelRepository repository;

    public UserProjection(UserReadModelRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(UserRegisteredEvent event) {
        var model = new UserReadModel(
            event.userId(),
            event.lastName() + " " + event.firstName(),  // 姓 名 に修正
            event.email()
        );
        repository.save(model);
    }
}
