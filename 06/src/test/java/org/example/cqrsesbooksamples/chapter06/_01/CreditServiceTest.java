package org.example.cqrsesbooksamples.chapter06._01;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.junit.jupiter.api.Test;

class CreditServiceTest {

    private final CreditApi creditApi = mock(CreditApi.class);
    private final EventGateway eventGateway = mock(EventGateway.class);
    private final CreditService service =
        new CreditService(creditApi, mock(QueryGateway.class), eventGateway);

    @Test
    void 決済処理IDを冪等キーとして外部APIへ渡して承認イベントへ翻訳する() {
        var processId = new PaymentProcessId();
        var accountId = new AccountId(UUID.randomUUID());
        var amount = new BigDecimal("1000");
        var request = new CreditMakePaymentRequest(processId, accountId, amount);
        when(creditApi.makePayment(request))
            .thenReturn(new CreditMakePaymentResponse(200, true));

        service.handle(new ExecuteCreditPaymentCommand(processId, accountId, amount));

        verify(creditApi).makePayment(request);
        verify(eventGateway).publish(new CreditAcceptedEvent(processId));
    }

    @Test
    void 非200応答を失敗イベントへ翻訳する() {
        var command = new ExecuteCreditPaymentCommand(
            new PaymentProcessId(),
            new AccountId(UUID.randomUUID()),
            new BigDecimal("1000"));
        when(creditApi.makePayment(new CreditMakePaymentRequest(
            command.paymentProcessId(), command.accountId(), command.amount())))
            .thenReturn(new CreditMakePaymentResponse(503, false));

        service.handle(command);

        verify(eventGateway).publish(new CreditMakePaymentFailedEvent(command, null));
    }
}
