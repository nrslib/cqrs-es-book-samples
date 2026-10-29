// 紙面照合: 2.22
package org.example.cqrsesbooksamples.chapter02._2;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;


@Component
public class UserQueryHandler {
    private final UserDataModelRepository repository;  // リードモデルのリポジトリ

    public UserQueryHandler(UserDataModelRepository repository) {
        this.repository = repository;
    }

    @QueryHandler
    public FindUserQueryResult handle(FindUserQuery query) {
        var data = repository.findById(query.userId());
        // ユーザIDでデータベースを検索し、結果を包んで返す
        return new FindUserQueryResult(data);
    }
}
