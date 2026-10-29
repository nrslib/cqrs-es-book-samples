package org.example.cqrsesbooksamples.chapter06._10;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.example.cqrsesbooksamples.chapter06._01.AccountId;
import org.example.cqrsesbooksamples.chapter06._01.ExecuteCreditPaymentCommand;
import org.example.cqrsesbooksamples.chapter06._01.PaymentProcessId;
import org.junit.jupiter.api.Test;

class CreditRetryExecutionHandlerTest {

    private final CommandGateway commandGateway = mock(CommandGateway.class);
    private final CreditRetryExecutionHandler handler =
        new CreditRetryExecutionHandler(commandGateway);

    @Test
    void 予約された再試行イベントのコマンドをそのまま再送する() {
        var command = new ExecuteCreditPaymentCommand(
            new PaymentProcessId(),
            new AccountId(UUID.randomUUID()),
            new BigDecimal("1000"),
            3);

        handler.on(new ExternalRetryRequestedEvent(command));

        verify(commandGateway).send(command);
    }
}
