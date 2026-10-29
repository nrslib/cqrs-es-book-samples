package org.example.cqrsesbooksamples.chapter03._33;

import java.util.concurrent.CompletableFuture;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserDeletionController {
    private final CommandGateway commandGateway;
    private final PersonalInformationStore personalInfoStore;
    private final UserProjectionRepository userProjectionRepository;

    public UserDeletionController(CommandGateway commandGateway,
                                  PersonalInformationStore personalInfoStore,
                                  UserProjectionRepository userProjectionRepository) {
        this.commandGateway = commandGateway;
        this.personalInfoStore = personalInfoStore;
        this.userProjectionRepository = userProjectionRepository;
    }

    @DeleteMapping("/{userId}")
    public CompletableFuture<Void> deleteUser(@PathVariable String userId) {
        var projection = userProjectionRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        // 先に個人情報を削除
        personalInfoStore.delete(projection.getPersonalInfoId());

        // その後、削除を示すコマンドを送信
        var command = new DeleteUserCommand(userId);

        return commandGateway.send(command).thenApply(result -> null);
    }
}
