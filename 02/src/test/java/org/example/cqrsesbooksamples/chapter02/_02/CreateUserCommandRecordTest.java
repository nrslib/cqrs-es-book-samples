// 検証補助: 2.5
package org.example.cqrsesbooksamples.chapter02._2;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CreateUserCommandRecordTest {


    @Test
    void record型のコマンドが名前とメールアドレスを保持する() {
        var command = new CreateUserCommand("John", "john@example.com");

        assertThat(command.name()).isEqualTo("John");
        assertThat(command.email()).isEqualTo("john@example.com");
    }
}
