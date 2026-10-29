// 紙面照合: 2.48
package org.example.cqrsesbooksamples.chapter02._61;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;


@Component
@ProcessingGroup("user-projection")
public class UserProjection {
    private final UserReadModelRepository repository;

    public UserProjection(UserReadModelRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(UserRegisteredEvent event) {
        repository.save(new UserReadModel(event.userId(), event.name()));
    }
}
