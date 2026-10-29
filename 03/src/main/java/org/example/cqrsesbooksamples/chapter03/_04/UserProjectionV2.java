// 紙面照合: 3.3
package org.example.cqrsesbooksamples.chapter03._04;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;


// 新しいプロジェクション（v2）を別グループで起動
@Component
@ProcessingGroup("user-projection-v2")
public class UserProjectionV2 {
    private final UserReadModelV2Repository repository;

    public UserProjectionV2(UserReadModelV2Repository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(UserRegisteredEvent event) {
        // 新しいロジックで処理
        var model = new UserReadModelV2(
            event.userId(),
            event.fullName(),  // 新しい表示形式
            event.email(),
            event.registeredAt()  // 新しいプロパティ
        );
        repository.save(model);
    }
}
