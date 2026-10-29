package org.example.cqrsesbooksamples.chapter02._9;

import static org.assertj.core.api.Assertions.assertThat;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 識別子を持たないコマンド + CREATE_IF_MISSING は常に新規作成の経路に入る、
 * という書籍本文（02-cqrs-basic/02.md）の記述を実際に動かして検証する。
 * 同じ内容のコマンドを2回送っても既存集約への更新にはならず、別々の集約が作られる。
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter02_09;DB_CLOSE_DELAY=-1")
class CreateIfMissingWithoutIdentifierIntegrationTest {
    @Autowired
    private CommandGateway commandGateway;

    @Test
    void 識別子なしコマンドは常に新規作成の経路に入る() {
        CreateUserResult first = commandGateway.sendAndWait(new CreateUserCommand("John", "john@example.com"));
        CreateUserResult second = commandGateway.sendAndWait(new CreateUserCommand("John", "john@example.com"));

        assertThat(first.userId()).isNotNull();
        assertThat(second.userId()).isNotNull();
        assertThat(first.userId()).isNotEqualTo(second.userId());
    }
}
