package org.example.cqrsesbooksamples.chapter03._33;

import java.time.Instant;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
public class UserProjection {
    private final UserProjectionRepository repository;

    public UserProjection(UserProjectionRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(UserRegisteredEvent event) {
        repository.save(new UserProjectionEntity(
            event.userId(), event.personalInfoId(), event.displayName(), Instant.now()));
    }
}
