package org.example.cqrsesbooksamples.chapter06._09;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.example.cqrsesbooksamples.chapter06._01.AccountId;
import org.example.cqrsesbooksamples.chapter06._01.CreditAcceptedEvent;
import org.example.cqrsesbooksamples.chapter06._01.CreditApi;
import org.example.cqrsesbooksamples.chapter06._01.ExecuteCreditPaymentCommand;
import org.example.cqrsesbooksamples.chapter06._01.CreditMakePaymentResponse;
import org.example.cqrsesbooksamples.chapter06._01.PaymentProcessId;
import org.junit.jupiter.api.Test;

class RetryingCreditServiceTest {
    @Test
    void 一時的な失敗はリトライで回復してイベントが発行される() {
        var creditApi = mock(CreditApi.class);
        var eventGateway = mock(EventGateway.class);
        when(creditApi.makePayment(any()))
            .thenThrow(new RuntimeException("一時的な接続エラー"))
            .thenReturn(new CreditMakePaymentResponse(200, true));

        var service = new CreditService(creditApi, mock(QueryGateway.class), eventGateway);
        var processId = new PaymentProcessId();
        service.handle(new ExecuteCreditPaymentCommand(processId, new AccountId(UUID.randomUUID()), new BigDecimal("1000")));

        verify(creditApi, times(2)).makePayment(any());
        verify(eventGateway).publish(new CreditAcceptedEvent(processId));
    }
}
