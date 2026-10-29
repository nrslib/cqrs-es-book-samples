// 紙面照合: 10.4
package org.example.cqrsesbooksamples.chapter10._08;

import org.axonframework.modelling.command.TargetAggregateIdentifier;


// コマンドがHasTenantIdを実装し、複合IDへ委譲する
public record TenantScopedCommand(
    @TargetAggregateIdentifier OrderId orderId
) implements HasTenantId {
    @Override
    public String getTenantId() {
        return orderId.getTenantId();
    }
}
