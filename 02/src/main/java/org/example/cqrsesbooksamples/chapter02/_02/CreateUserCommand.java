// 紙面照合: 2.5
package org.example.cqrsesbooksamples.chapter02._2;


public record CreateUserCommand(
    String name,  // ユーザ名
    String email  // メールアドレス
) {}
