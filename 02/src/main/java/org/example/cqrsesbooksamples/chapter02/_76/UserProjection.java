// 紙面照合: 2.60
package org.example.cqrsesbooksamples.chapter02._76;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.springframework.stereotype.Component;


// リードモデル更新専用（リビルド対象）
@Component
@ProcessingGroup("user-projection")
public class UserProjection {
    private final UserReadModelRepository repository;

    public UserProjection(UserReadModelRepository repository) {
        this.repository = repository;
    }

    @ResetHandler
    public void reset() {
        repository.deleteAll();
    }

    @EventHandler
    public void on(UserRegisteredEvent event) {
        var model = new UserReadModel(
            event.userId(),
            event.name(),
            event.email()
        );
        repository.save(model);
    }
}
