package org.example.cqrsesbooksamples.chapter03._63;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

// 削除済み（旧 3.61）
@Component
@ProcessingGroup("user-projection")
public class UserProjection {
    private final UserReadModelRepository repository;

    public UserProjection(UserReadModelRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(UserRegisteredEvent event) {
        var model = new UserReadModel(
            event.userId(),
            event.name(),
            event.email()
        );
        repository.save(model);
        // リードモデルの更新とトラッキングトークンの更新が
        // 同一トランザクションで実行される
    }
}
