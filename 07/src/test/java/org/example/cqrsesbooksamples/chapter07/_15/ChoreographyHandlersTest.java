package org.example.cqrsesbooksamples.chapter07._15;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.example.cqrsesbooksamples.chapter07._01.CreditAcceptedEvent;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessAcceptedEvent;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessCompletedEvent;
import org.example.cqrsesbooksamples.chapter07.support.AccountId;
import org.example.cqrsesbooksamples.chapter07.support.CompletePaymentProcessCommand;
import org.example.cqrsesbooksamples.chapter07.support.ExecuteCreditPaymentCommand;
import org.example.cqrsesbooksamples.chapter07.support.SendPaymentCompletionNotificationCommand;
import org.example.cqrsesbooksamples.chapter07.support.PaymentMethod;
import org.example.cqrsesbooksamples.chapter07.support.PaymentProcessId;
import org.example.cqrsesbooksamples.chapter07.support.RequestBankTransferCommand;
import org.junit.jupiter.api.Test;

class ChoreographyHandlersTest {
    private final PaymentProcessId processId = new PaymentProcessId();
    private final AccountId accountId = new AccountId(UUID.randomUUID());
    private final BigDecimal amount = new BigDecimal("5000");

    @Test
    void クレジットハンドラは自分の決済方式のときだけ反応する() {
        var gateway = mock(CommandGateway.class);
        var handler = new CreditPaymentHandler(gateway);

        handler.on(new PaymentProcessAcceptedEvent(processId, accountId, amount, PaymentMethod.CREDIT));
        verify(gateway).send(new ExecuteCreditPaymentCommand(processId, accountId, amount));

        var gateway2 = mock(CommandGateway.class);
        var handler2 = new CreditPaymentHandler(gateway2);
        handler2.on(new PaymentProcessAcceptedEvent(processId, accountId, amount, PaymentMethod.BANK_TRANSFER));
        verifyNoInteractions(gateway2);
    }

    @Test
    void 銀行振込ハンドラは振込のときだけ反応する() {
        var gateway = mock(CommandGateway.class);
        var handler = new BankTransferHandler(gateway);

        handler.on(new PaymentProcessAcceptedEvent(processId, accountId, amount, PaymentMethod.BANK_TRANSFER));
        verify(gateway).send(new RequestBankTransferCommand(processId, accountId, amount));
    }

    @Test
    void 決済完了イベントで通知コマンドが送られる() {
        var gateway = mock(CommandGateway.class);
        var handler = new PaymentNotificationHandler(gateway);

        handler.on(new PaymentProcessCompletedEvent(processId));
        verify(gateway).send(new SendPaymentCompletionNotificationCommand(processId));
    }

    @Test
    void 決済承認で完了コマンドが送られる() {
        var gateway = mock(CommandGateway.class);
        var handler = new CreditPaymentHandler(gateway);

        handler.on(new CreditAcceptedEvent(processId));
        verify(gateway).send(new CompletePaymentProcessCommand(processId));
    }
}
