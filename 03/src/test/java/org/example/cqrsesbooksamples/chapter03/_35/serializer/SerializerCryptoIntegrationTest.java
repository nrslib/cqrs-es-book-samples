// 検証補助: 3.40
package org.example.cqrsesbooksamples.chapter03._35.serializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.messaging.unitofwork.DefaultUnitOfWork;
import org.axonframework.modelling.command.Repository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(classes = SerializerCryptoTestApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:serializer_crypto;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "axon.serializer.events=jackson",
        "axon.serializer.messages=jackson"
})
class SerializerCryptoIntegrationTest {
    @Autowired
    private CommandGateway commands;
    @Autowired
    private Configuration configuration;
    @Autowired
    private InMemoryCryptoEngine cryptoEngine;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private ObjectMapper mapper;

    @Test
    void イベントストアで暗号化し鍵破棄後も個人情報なしで集約をロードできる() throws Exception {
        String userId = UUID.randomUUID().toString();
        cryptoEngine.createKey(userId);
        commands.sendAndWait(new UserAggregate.RegisterUserCommand(userId, "太郎", "taro@example.com"));

        byte[] stored = payload(userId);
        JsonNode json = mapper.readTree(stored);
        assertThat(json.path("userId").asText()).isEqualTo(userId);
        assertThat(json.path("name").isObject()).isTrue();
        assertThat(json.path("email").isObject()).isTrue();
        assertThat(json.path("name").toString()).doesNotContain("太郎");
        assertThat(json.path("email").toString()).doesNotContain("taro@example.com");
        assertThat(load(userId)).isEqualTo(new UserAggregate.UserState(userId, "太郎", "taro@example.com"));

        cryptoEngine.destroyKey(userId);

        assertThat(load(userId)).isEqualTo(new UserAggregate.UserState(userId, null, null));
        assertThat(payload(userId)).isEqualTo(stored);
    }

    @Test
    void 鍵が用意されていなければコマンドは失敗し平文イベントも保存されない() {
        String userId = UUID.randomUUID().toString();
        assertThatThrownBy(() -> commands.sendAndWait(
                new UserAggregate.RegisterUserCommand(userId, "太郎", "taro@example.com")))
                .isInstanceOf(RuntimeException.class);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM domain_event_entry WHERE aggregate_identifier = ?", Integer.class, userId))
                .isZero();
    }

    private byte[] payload(String userId) {
        return jdbc.queryForObject(
                "SELECT payload FROM domain_event_entry WHERE aggregate_identifier = ?", byte[].class, userId);
    }

    private UserAggregate.UserState load(String userId) throws Exception {
        Repository<UserAggregate> repository = configuration.repository(UserAggregate.class);
        var unitOfWork = DefaultUnitOfWork.startAndGet(null);
        return unitOfWork.executeWithResult(() -> repository.load(userId).invoke(UserAggregate::state))
                .getPayload();
    }
}
