package org.example.cqrsesbooksamples.chapter06._10;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.DisallowReplay;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

// 削除済み（旧 6.11）
@Component
public record CreditRetryExecutionHandler(CommandGateway commandGateway) {
    @DisallowReplay
    @EventHandler
    public void on(ExternalRetryRequestedEvent event) {
        commandGateway.send(event.command());
    }
}
