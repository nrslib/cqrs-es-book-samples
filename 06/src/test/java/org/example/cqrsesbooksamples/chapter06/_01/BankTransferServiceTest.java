package org.example.cqrsesbooksamples.chapter06._01;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Test;

class BankTransferServiceTest {

    private final BankTransferApi bankApi = mock(BankTransferApi.class);
    private final EventGateway eventGateway = mock(EventGateway.class);
    private final BankTransferService service =
        new BankTransferService(bankApi, eventGateway);

    @Test
    void 銀行APIの振込IDを依頼登録イベントへ翻訳する() {
        var command = command();
        var request = new BankTransferRequest(command.accountId(), command.amount());
        when(bankApi.requestTransfer(request))
            .thenReturn(new BankTransferResponse("transfer-001"));

        service.handle(command);

        verify(bankApi).requestTransfer(request);
        verify(eventGateway).publish(
            new BankTransferRequestedEvent(command.paymentProcessId(), "transfer-001"));
    }

    @Test
    void 銀行API例外を振込依頼失敗イベントへ翻訳する() {
        var command = command();
        var request = new BankTransferRequest(command.accountId(), command.amount());
        when(bankApi.requestTransfer(request))
            .thenThrow(new BankApiException("bank api unavailable"));

        service.handle(command);

        verify(eventGateway).publish(new BankTransferRequestFailedEvent(
            command.paymentProcessId(), "bank api unavailable"));
    }

    private RequestBankTransferCommand command() {
        return new RequestBankTransferCommand(
            new PaymentProcessId(),
            new AccountId(UUID.randomUUID()),
            new BigDecimal("10000"));
    }
}
