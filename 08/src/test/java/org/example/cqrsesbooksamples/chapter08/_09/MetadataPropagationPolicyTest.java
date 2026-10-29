// 紙面照合: 8.10
package org.example.cqrsesbooksamples.chapter08._09;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.MetaData;
import org.example.cqrsesbooksamples.chapter08._07.OrderPaidEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class MetadataPropagationPolicyTest {
    private CommandGateway commandGateway;
    private OrderFulfillmentPolicy policy;

    @BeforeEach
    void setUp() {
        commandGateway = mock(CommandGateway.class);
        policy = new OrderFulfillmentPolicy(commandGateway);
    }


    @Test
    void shouldPropagateMetadata() {
        var metadata = MetaData.with("batchId", "BATCH-2024-001");

        policy.on(new OrderPaidEvent("ORDER-001"), metadata);

        var captor = ArgumentCaptor.forClass(CommandMessage.class);
        verify(commandGateway).send(captor.capture());
        assertEquals("BATCH-2024-001", captor.getValue().getMetaData().get("batchId"));
    }
}
