// 検証補助: 10.4
package org.example.cqrsesbooksamples.chapter10._08;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.junit.jupiter.api.Test;


class TenantAwareRoutingStrategyTest {
    private final TenantAwareRoutingStrategy strategy = new TenantAwareRoutingStrategy();

    record PlainCommand(@TargetAggregateIdentifier String aggregateId) {}

    @Test
    void テナントを持つコマンドはテナントIDでルーティングされる() {
        var command = new TenantScopedCommand(new OrderId("tenant-A", UUID.randomUUID()));

        var routingKey = strategy.getRoutingKey(GenericCommandMessage.asCommandMessage(command));

        assertThat(routingKey).isEqualTo("tenant-A");
    }

    @Test
    void テナントを持たないコマンドは既定の集約IDルーティングに委ねられる() {
        var command = new PlainCommand("aggregate-1");

        var routingKey = strategy.getRoutingKey(GenericCommandMessage.asCommandMessage(command));

        assertThat(routingKey).isEqualTo("aggregate-1");
    }

    @Test
    void 同じテナントのコマンドは集約が違っても同じキーになる() {
        var first = new TenantScopedCommand(new OrderId("tenant-A", UUID.randomUUID()));
        var second = new TenantScopedCommand(new OrderId("tenant-A", UUID.randomUUID()));

        assertThat(strategy.getRoutingKey(GenericCommandMessage.asCommandMessage(first)))
            .isEqualTo(strategy.getRoutingKey(GenericCommandMessage.asCommandMessage(second)));
    }
}
