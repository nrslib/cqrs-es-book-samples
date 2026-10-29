// 紙面照合: 2.57
package org.example.cqrsesbooksamples.chapter02._73;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.springframework.stereotype.Component;


@Component
@ProcessingGroup("user-projection")
public class UserProjection {
    private final UserReadModelRepository repository;

    public UserProjection(UserReadModelRepository repository) {
        this.repository = repository;
    }

    @ResetHandler
    public void reset() {
        // リビルド開始前にリードモデルをクリア
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

    @EventHandler
    public void on(UserUpdatedEvent event) {
        repository.findById(event.userId())
            .ifPresent(model -> {
                model.updateName(event.name());
                model.updateEmail(event.email());
                repository.save(model);
            });
    }
}
