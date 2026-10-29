package org.example.cqrsesbooksamples.chapter03._11;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chapter03_11;DB_CLOSE_DELAY=-1",
        "maintenance.mode=true"
})
class MaintenanceModeTest {
    @Autowired
    private CommandGateway commandGateway;

    @Test
    void メンテナンスモード中はコマンドが拒否される() {
        assertThatThrownBy(() -> commandGateway.sendAndWait(new PingCommand("any")))
            .isInstanceOf(MaintenanceModeException.class);
    }
}
