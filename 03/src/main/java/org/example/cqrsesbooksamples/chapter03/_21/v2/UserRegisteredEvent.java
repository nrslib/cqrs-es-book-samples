// 紙面照合: 3.27
package org.example.cqrsesbooksamples.chapter03._21.v2;

import java.time.LocalDate;


// Version 2.0
public record UserRegisteredEvent(
    String userId,
    String email,
    String passwordHash,  // フィールド名の変更
    LocalDate birthDate  // 新しい項目
) {}
