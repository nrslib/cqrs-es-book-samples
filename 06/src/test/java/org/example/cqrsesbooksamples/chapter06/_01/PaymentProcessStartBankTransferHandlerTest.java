package org.example.cqrsesbooksamples.chapter06._01;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Test;

class PaymentProcessStartBankTransferHandlerTest {

    private final CommandGateway commandGateway = mock(CommandGateway.class);
    private final PaymentProcessStartBankTransferHandler handler =
        new PaymentProcessStartBankTransferHandler(commandGateway);

    @Test
    void 銀行振込なら振込依頼コマンドを送る() {
        var processId = new PaymentProcessId();
        var accountId = new AccountId(UUID.randomUUID());
        var amount = new BigDecimal("10000");

        handler.on(new PaymentProcessAcceptedEvent(
            processId, accountId, amount, PaymentMethod.BANK_TRANSFER));

        verify(commandGateway).send(
            new RequestBankTransferCommand(processId, accountId, amount));
    }

    @Test
    void クレジット決済なら振込依頼コマンドを送らない() {
        handler.on(new PaymentProcessAcceptedEvent(
            new PaymentProcessId(),
            new AccountId(UUID.randomUUID()),
            new BigDecimal("10000"),
            PaymentMethod.CREDIT));

        verifyNoInteractions(commandGateway);
    }
}
