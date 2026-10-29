// 紙面照合: 3.35
package org.example.cqrsesbooksamples.chapter03._21.weakschema.after;

import jakarta.annotation.Nullable;


// 変更後 - 同じクラスのままオプショナルフィールドを追加
public record UserRegisteredEvent(
    String userId,
    String email,
    @Nullable String phoneNumber  // 明示的にオプショナル
) {}
