// 紙面照合: 3.42
package org.example.cqrsesbooksamples.chapter03._58;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;


@Component
@ProcessingGroup("user-projection")
public class UserProjection {
    private static final Logger log = LoggerFactory.getLogger(UserProjection.class);

    private final UserReadModelRepository repository;

    public UserProjection(UserReadModelRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(UserRegisteredEvent event) {
        try {
            var model = new UserReadModel(
                event.userId(),
                event.name(),
                event.email()
            );
            repository.save(model);
        } catch (DataAccessException e) {
            // データベースエラーをログに記録
            log.error("ユーザの保存に失敗しました: " + event.userId(), e);
            // 例外を再スロー（その後の扱いはエラーハンドラの設定で決まる）
            throw e;
        }
    }
}
