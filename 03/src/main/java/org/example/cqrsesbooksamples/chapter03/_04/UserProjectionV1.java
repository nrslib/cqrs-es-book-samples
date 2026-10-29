// 紙面照合: 3.3
package org.example.cqrsesbooksamples.chapter03._04;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;


// 既存のプロジェクション（v1）は稼働中
@Component
@ProcessingGroup("user-projection-v1")
public class UserProjectionV1 {
    private final UserReadModelV1Repository repository;

    public UserProjectionV1(UserReadModelV1Repository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(UserRegisteredEvent event) {
        var model = new UserReadModelV1(
            event.userId(),
            event.name(),
            event.email()
        );
        repository.save(model);
    }
}
