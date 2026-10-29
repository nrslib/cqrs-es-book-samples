// 紙面照合: 2.56
package org.example.cqrsesbooksamples.chapter02._71;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;


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
            event.firstName() + " " + event.lastName(),  // 誤って名を先に連結していた
            event.email()
        );
        repository.save(model);
    }
}
