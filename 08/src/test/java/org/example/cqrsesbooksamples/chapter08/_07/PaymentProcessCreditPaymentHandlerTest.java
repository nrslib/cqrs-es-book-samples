// 紙面照合: 8.8
package org.example.cqrsesbooksamples.chapter08._07;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.example.cqrsesbooksamples.chapter06._01.AccountId;
import org.example.cqrsesbooksamples.chapter06._01.PaymentMethod;
import org.example.cqrsesbooksamples.chapter06._01.PaymentProcessAcceptedEvent;
import org.example.cqrsesbooksamples.chapter06._01.PaymentProcessCreditPaymentHandler;
import org.example.cqrsesbooksamples.chapter06._01.PaymentProcessId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PaymentProcessCreditPaymentHandlerTest {
    private CommandGateway commandGateway;

    @BeforeEach
    void setUp() {
        commandGateway = mock(CommandGateway.class);
    }


    @Test
    void shouldNotExecuteCreditPaymentForBankTransfer() {
        var handler = new PaymentProcessCreditPaymentHandler(commandGateway);

        handler.on(new PaymentProcessAcceptedEvent(
            new PaymentProcessId(),
            new AccountId(UUID.randomUUID()),
            BigDecimal.valueOf(1000),
            PaymentMethod.BANK_TRANSFER));

        verifyNoInteractions(commandGateway);
    }
}
