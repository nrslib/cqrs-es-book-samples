// 紙面照合: 2.59
package org.example.cqrsesbooksamples.chapter02._75;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ReplayStatus;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("user-projection-replay-aware")
public class UserProjection {
    private final UserReadModelRepository repository;
    private final NotificationService notificationService;

    public UserProjection(UserReadModelRepository repository, NotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
    }


    @EventHandler
    public void on(UserRegisteredEvent event, ReplayStatus replayStatus) {
        var model = new UserReadModel(
            event.userId(),
            event.name(),
            event.email()
        );
        repository.save(model);

        // リプレイ中は外部通知をスキップ
        if (!replayStatus.isReplay()) {
            notificationService.sendWelcomeEmail(event.email());
        }
    }
}
