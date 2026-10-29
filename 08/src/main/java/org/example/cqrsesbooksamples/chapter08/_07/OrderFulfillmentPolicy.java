package org.example.cqrsesbooksamples.chapter08._07;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
public record OrderFulfillmentPolicy(CommandGateway commandGateway) {
    @EventHandler
    public void on(OrderPaidEvent event) {
        commandGateway.send(new InitiateShipmentCommand(event.orderId()));
    }

    @EventHandler
    public void on(InventoryReservedEvent event) {
        commandGateway.send(new NotifyCustomerCommand(event.orderId()));
    }
}
