// 紙面照合: 2.47
package org.example.cqrsesbooksamples.chapter02._60;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("user-management")
@Order(1)
public class UserProjection {
    private final UserReadModelRepository repository;

    public UserProjection(UserReadModelRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(UserRegisteredEvent event) {
        // 先に実行: リードモデルを更新
        repository.save(new UserReadModel(event.userId(), event.name()));
    }
}
