// 紙面照合: 8.7
package org.example.cqrsesbooksamples.chapter08._07;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class OrderFulfillmentPolicyTest {
    private CommandGateway commandGateway;
    private OrderFulfillmentPolicy policy;

    @BeforeEach
    void setUp() {
        commandGateway = mock(CommandGateway.class);
        policy = new OrderFulfillmentPolicy(commandGateway);
    }


    @Test
    void shouldTriggerShipmentOnOrderPaid() {
        policy.on(new OrderPaidEvent("ORDER-001"));

        verify(commandGateway).send(new InitiateShipmentCommand("ORDER-001"));
    }


}
