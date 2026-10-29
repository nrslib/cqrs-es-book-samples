// 紙面照合: 10.4
package org.example.cqrsesbooksamples.chapter10._08;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy;
import org.axonframework.commandhandling.distributed.RoutingStrategy;
import org.springframework.stereotype.Component;


@Component
public class TenantAwareRoutingStrategy implements RoutingStrategy {
    private final RoutingStrategy fallback = AnnotationRoutingStrategy.defaultStrategy();

    @Override
    public String getRoutingKey(CommandMessage<?> command) {
        if (command.getPayload() instanceof HasTenantId hasTenantId) {
            return hasTenantId.getTenantId();
        }
        return fallback.getRoutingKey(command);
    }
}
