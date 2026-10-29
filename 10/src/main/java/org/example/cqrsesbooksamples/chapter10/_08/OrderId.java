// 紙面照合: 10.4
package org.example.cqrsesbooksamples.chapter10._08;

import java.util.UUID;


public record OrderId(String tenantId, UUID orderId) implements HasTenantId {
    @Override
    public String getTenantId() {
        return tenantId;
    }
}
