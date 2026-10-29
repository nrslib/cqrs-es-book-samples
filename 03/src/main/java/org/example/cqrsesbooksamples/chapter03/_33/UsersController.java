// 紙面照合: 3.37
package org.example.cqrsesbooksamples.chapter03._33;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/users")
public class UsersController {
    private final PersonalInformationStore personalInfoStore;
    private final CommandGateway commandGateway;

    public UsersController(PersonalInformationStore personalInfoStore, CommandGateway commandGateway) {
        this.personalInfoStore = personalInfoStore;
        this.commandGateway = commandGateway;
    }

    @PostMapping
    public CompletableFuture<String> registerUser(@RequestBody RegistrationRequest request) {
        // 先に個人情報を保存（ストアが暗号化する）
        var personalInfo = new PersonalInformation(
            request.email(),
            request.phoneNumber(),
            request.address()
        );
        var personalInfoId = personalInfoStore.store(personalInfo);

        // 保存された識別子を使ってコマンドを作成
        var command = new RegisterUserCommand(
            UUID.randomUUID().toString(),  // userId
            personalInfoId,                // 個人情報の参照ID
            request.displayName()          // 公開表示名
        );

        return commandGateway.send(command);
    }
}
