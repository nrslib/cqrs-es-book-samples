// 紙面照合: 2.4, 2.25, 2.34
package org.example.cqrsesbooksamples.chapter02._2;

import java.util.concurrent.CompletableFuture;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/users")
public class UserController {
    private final CommandGateway commandGateway;  // コマンドを送信するためのゲートウェイ

    public UserController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @PostMapping
    public CompletableFuture<String> createUser(@RequestBody CreateUserRequest request) {
        // リクエストからコマンドオブジェクトを作成
        var command = new CreateUserCommand(
            request.name(),
            request.email()
        );
        return commandGateway.send(command);  // コマンドを非同期で送信
    }


    @PutMapping("/{userId}")
    public void put(
        @PathVariable String userId,  // 更新対象のユーザID
        @RequestBody UpdateUserRequest request  // 更新内容
    ) {
        // コマンドオブジェクトを作成
        var command = new UpdateUserCommand(
            userId,
            request.name(),
            request.email()
        );
        // コマンドを同期的に送信
        commandGateway.sendAndWait(command);
    }


    @DeleteMapping("/{userId}")
    public void delete(@PathVariable String userId) {
        // 削除コマンドを作成
        var command = new DeleteUserCommand(userId);

        // コマンドを同期的に送信
        commandGateway.sendAndWait(command);
    }
}
