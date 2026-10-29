package org.example.cqrsesbooksamples.chapter02._62.named;

import org.axonframework.config.ProcessingGroup;
import org.springframework.stereotype.Component;

// 削除済み（旧 2.62）
// ProcessingGroupを指定する場合
@Component
@ProcessingGroup("user-projection")
public class UserProjection {
    // ProcessingGroup名は "user-projection" になる
}
