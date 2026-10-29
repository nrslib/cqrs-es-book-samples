// 検証補助: 2.4, 2.23, 2.25, 2.34
package org.example.cqrsesbooksamples.chapter02._2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class UserControllersTest {

    // 削除済み（旧 2.16）
    @Test
    void 作成リクエストをコマンドへ変換して非同期送信する() {
        var commandGateway = mock(CommandGateway.class);
        when(commandGateway.send(any(CreateUserCommand.class)))
            .thenReturn(CompletableFuture.completedFuture("user-123"));
        var controller = new UserController(commandGateway);

        var result = controller.createUser(new CreateUserRequest("John", "john@example.com"));

        var captor = ArgumentCaptor.forClass(CreateUserCommand.class);
        verify(commandGateway).send(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new CreateUserCommand("John", "john@example.com"));
        assertThat(result.join()).isEqualTo("user-123");
    }


    @Test
    void ユーザ検索クエリを実行してレスポンスへ変換する() {
        var queryGateway = mock(QueryGateway.class);
        var user = new UserDataModel("user-123", "John", "john@example.com");
        when(queryGateway.query(any(FindUserQuery.class), eq(FindUserQueryResult.class)))
            .thenReturn(CompletableFuture.completedFuture(new FindUserQueryResult(Optional.of(user))));
        var controller = new UsersController(queryGateway);

        var response = controller.get("user-123");

        var captor = ArgumentCaptor.forClass(FindUserQuery.class);
        verify(queryGateway).query(captor.capture(), eq(FindUserQueryResult.class));
        assertThat(captor.getValue()).isEqualTo(new FindUserQuery("user-123"));
        assertThat(response).isEqualTo(new UserGetResponse("user-123", "John"));
    }


    @Test
    void 更新リクエストを対象ID付きコマンドへ変換して同期送信する() {
        var commandGateway = mock(CommandGateway.class);
        var controller = new UserController(commandGateway);

        controller.put("user-123", new UpdateUserRequest("John Doe", "john.doe@example.com"));

        verify(commandGateway).sendAndWait(new UpdateUserCommand(
            "user-123", "John Doe", "john.doe@example.com"));
    }


    @Test
    void 削除対象IDをコマンドへ変換して同期送信する() {
        var commandGateway = mock(CommandGateway.class);
        var controller = new UserController(commandGateway);

        controller.delete("user-123");

        verify(commandGateway).sendAndWait(new DeleteUserCommand("user-123"));
    }
}
