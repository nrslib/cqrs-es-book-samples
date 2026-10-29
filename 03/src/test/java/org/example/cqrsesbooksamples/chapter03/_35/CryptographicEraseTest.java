package org.example.cqrsesbooksamples.chapter03._35;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.matchers.Matchers;
import org.junit.jupiter.api.Test;

class CryptographicEraseTest {
    @Test
    void イベントには暗号化された個人情報だけが残り鍵の削除で復号不能になる() {
        KeyVault.createKey("key-user-1");

        var fixture = new AggregateTestFixture<>(UserAggregateAdapter.class);
        var captured = new UserRegisteredEvent[1];

        fixture.givenNoPriorActivity()
            .when(new RegisterUserCommand("user-1", "key-user-1", new PersonalData("Taro", "taro@example.com")))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(Matchers.exactSequenceOf(
                Matchers.messageWithPayload(Matchers.predicate(
                    (UserRegisteredEvent event) -> {
                        captured[0] = event;
                        return !event.encryptedData().contains("taro@example.com");
                    }))));

        // 鍵があるあいだは復号できる
        String decrypted = KeyVault.decrypt("key-user-1", captured[0].encryptedData());
        assertThat(decrypted).isEqualTo("Taro,taro@example.com");

        // 鍵を削除すると、イベントを消さずに個人情報へのアクセスだけが失われる
        KeyVault.deleteKey("key-user-1");
        assertThatThrownBy(() -> KeyVault.decrypt("key-user-1", captured[0].encryptedData()))
            .isInstanceOf(IllegalStateException.class);
    }
}
