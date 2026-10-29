// 紙面照合: 2.1
package org.example.cqrsesbooksamples.chapter02._1;

import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;


@Aggregate
public class UserAggregate {
    @AggregateIdentifier
    private String userId;  // 集約を一意に識別するID

    public UserAggregate() {
        // デフォルトコンストラクタが必要
        // イベントソーシングでの再構築時に使用される
    }
}
