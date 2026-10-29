// 紙面照合: 3.4
package org.example.cqrsesbooksamples.chapter03._04;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


// v1のクエリハンドラ（既定で有効）
@Component
@ConditionalOnProperty(name = "projection.active", havingValue = "v1", matchIfMissing = true)
public class UserQueryHandlerV1 {
    private final UserReadModelV1Repository repository;

    public UserQueryHandlerV1(UserReadModelV1Repository repository) {
        this.repository = repository;
    }

    @QueryHandler
    public UserSummary handle(GetUserQuery query) {
        return repository.findById(query.userId())
            .map(model -> new UserSummary(model.getUserId(), model.getName()))
            .orElseThrow();
    }
}
