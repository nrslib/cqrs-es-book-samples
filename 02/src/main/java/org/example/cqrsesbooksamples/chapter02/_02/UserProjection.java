// 紙面照合: 2.18, 2.33, 2.43
package org.example.cqrsesbooksamples.chapter02._2;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;


@Component
public class UserProjection {
    private final UserDataModelRepository repository;

    public UserProjection(UserDataModelRepository repository) {
        this.repository = repository;
    }


    @EventHandler
    public void on(UserCreatedEvent event) {
        // イベントからリードモデルを作成
        var model = new UserDataModel(
            event.userId(),
            event.name(),
            event.email()
        );
        // データベースに保存
        repository.save(model);
    }


    @EventHandler
    public void on(UserUpdatedEvent event) {
        // ユーザIDでデータベースから既存のユーザを検索
        var user = repository.findById(event.userId())
            .orElseThrow();

        // イベントのデータでユーザ情報を更新
        if (event.name() != null) {
            user.setName(event.name());
        }
        if (event.email() != null) {
            user.setEmail(event.email());
        }

        // 更新したユーザ情報を保存
        repository.save(user);
    }


    @EventHandler
    public void on(UserDeletedEvent event) {
        // ユーザIDを指定してデータベースから物理的に削除
        repository.deleteById(event.userId());
    }
}
