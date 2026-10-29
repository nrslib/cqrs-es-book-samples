// 紙面照合: 2.23
package org.example.cqrsesbooksamples.chapter02._2;

import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/users")
public class UsersController {
    private final QueryGateway queryGateway;  // クエリを実行するためのゲートウェイ

    public UsersController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @GetMapping("/{userId}")
    public UserGetResponse get(@PathVariable String userId) {
        // ユーザ検索クエリを作成
        var query = new FindUserQuery(userId);

        // クエリを実行して結果を取得
        var result = queryGateway.query(query, FindUserQueryResult.class).join();

        // 結果が存在しない場合は404エラー
        var user = result.user().orElseThrow(NotFoundException::new);

        // レスポンスを作成して返却
        return new UserGetResponse(user.getUserId(), user.getName());
    }
}
