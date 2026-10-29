package org.example.cqrsesbooksamples.chapter02._6;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * POJOで定義したコマンド（旧 2.6）の検証。
 * record版と等価な保持・取得ができることを確かめる。
 */
class CreateUserCommandTest {

    @Test
    void コンストラクタで設定した値をゲッターで取得できる() {
        var command = new CreateUserCommand("John", "john@example.com");

        assertThat(command.getName()).isEqualTo("John");
        assertThat(command.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void セッターで値を差し替えられる() {
        var command = new CreateUserCommand("John", "john@example.com");

        command.setName("Jane");
        command.setEmail("jane@example.com");

        assertThat(command.getName()).isEqualTo("Jane");
        assertThat(command.getEmail()).isEqualTo("jane@example.com");
    }
}
