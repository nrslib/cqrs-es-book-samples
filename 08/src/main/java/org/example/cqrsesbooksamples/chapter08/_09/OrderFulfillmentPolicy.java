// 紙面照合: 8.9
package org.example.cqrsesbooksamples.chapter08._09;

import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.MetaData;
import org.example.cqrsesbooksamples.chapter08._07.InitiateShipmentCommand;
import org.example.cqrsesbooksamples.chapter08._07.OrderPaidEvent;
import org.springframework.stereotype.Component;


@Component
public record OrderFulfillmentPolicy(CommandGateway commandGateway) {
    @EventHandler
    public void on(OrderPaidEvent event, MetaData metaData) {
        commandGateway.send(
            GenericCommandMessage.asCommandMessage(new InitiateShipmentCommand(event.orderId()))
                .andMetaData(MetaData.with("batchId", metaData.get("batchId")))
        );
    }
}
