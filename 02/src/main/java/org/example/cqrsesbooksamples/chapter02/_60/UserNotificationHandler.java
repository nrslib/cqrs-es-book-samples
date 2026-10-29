// 紙面照合: 2.47
package org.example.cqrsesbooksamples.chapter02._60;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("user-management")
@Order(2)
public class UserNotificationHandler {
    private final NotificationService notificationService;

    public UserNotificationHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventHandler
    public void on(UserRegisteredEvent event) {
        // 後に実行: メールを送信
        // リードモデルがすでに存在することを前提にできる
        notificationService.sendWelcomeEmail(event.email());
    }
}
