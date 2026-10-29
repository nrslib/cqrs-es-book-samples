// 検証補助: 10.1, 10.2
package org.example.cqrsesbooksamples.chapter10._04;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy;
import org.axonframework.commandhandling.distributed.ConsistentHash;
import org.axonframework.commandhandling.distributed.SimpleMember;
import org.axonframework.commandhandling.distributed.commandfilter.AcceptAll;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.junit.jupiter.api.Test;

/**
 * Axon既定のシャーディング（AnnotationRoutingStrategy + ConsistentHash）の挙動を検証する。
 */
class DefaultRoutingStrategyTest {
    record UpdateOrderCommand(@TargetAggregateIdentifier String orderId) {}


    @Test
    void 既定戦略はコマンドの集約識別子をルーティングキーとして取り出す() {
        var strategy = AnnotationRoutingStrategy.defaultStrategy();
        var command = GenericCommandMessage.asCommandMessage(new UpdateOrderCommand("order-123"));

        assertThat(strategy.getRoutingKey(command)).isEqualTo("order-123");
    }


    @Test
    void ConsistentHashは同じルーティングキーを常に同じノードへ写す() {
        var node1 = new SimpleMember<>("node1", "endpoint1", false, null);
        var node2 = new SimpleMember<>("node2", "endpoint2", false, null);
        var node3 = new SimpleMember<>("node3", "endpoint3", false, null);
        var hash = new ConsistentHash()
            .with(node1, 10, AcceptAll.INSTANCE)
            .with(node2, 10, AcceptAll.INSTANCE)
            .with(node3, 10, AcceptAll.INSTANCE);
        var command = GenericCommandMessage.asCommandMessage(new UpdateOrderCommand("order-123"));

        // 同じキーは何度問い合わせても同じノードに写る
        var first = hash.getMember("order-123", command).orElseThrow();
        var second = hash.getMember("order-123", command).orElseThrow();
        assertThat(second.name()).isEqualTo(first.name());

        // 別ノードが抜けても、残ったノードに割り当て済みのキーは動かない
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            keys.add("order-" + i);
        }
        var before = keys.stream()
            .filter(key -> hash.getMember(key, command).orElseThrow().name().equals("node1"))
            .toList();
        var without2 = hash.without(node2);
        for (String key : before) {
            assertThat(without2.getMember(key, command).orElseThrow().name()).isEqualTo("node1");
        }
    }
}
