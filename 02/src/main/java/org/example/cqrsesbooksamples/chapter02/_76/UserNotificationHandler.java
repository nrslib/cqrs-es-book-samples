// 紙面照合: 2.60
package org.example.cqrsesbooksamples.chapter02._76;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;


// 外部通知専用（リビルド対象外）
@Component
@ProcessingGroup("user-notification")
public class UserNotificationHandler {
    private final NotificationService notificationService;

    public UserNotificationHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventHandler
    public void on(UserRegisteredEvent event) {
        notificationService.sendWelcomeEmail(event.email());
    }
}
