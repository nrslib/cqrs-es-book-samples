// 検証補助: 3.37
package org.example.cqrsesbooksamples.chapter03._33;


// イベントには個人情報の参照IDのみを含める
public record UserRegisteredEvent(
    String userId,
    String personalInfoId,
    String displayName
) {}
