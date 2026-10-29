// 紙面照合: 3.4
package org.example.cqrsesbooksamples.chapter03._04;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


// v2のクエリハンドラ（projection.active=v2 で有効化）
@Component
@ConditionalOnProperty(name = "projection.active", havingValue = "v2")
public class UserQueryHandlerV2 {
    private final UserReadModelV2Repository repository;

    public UserQueryHandlerV2(UserReadModelV2Repository repository) {
        this.repository = repository;
    }

    @QueryHandler
    public UserSummary handle(GetUserQuery query) {
        return repository.findById(query.userId())
            .map(model -> new UserSummary(model.getUserId(), model.getFullName()))
            .orElseThrow();
    }
}
