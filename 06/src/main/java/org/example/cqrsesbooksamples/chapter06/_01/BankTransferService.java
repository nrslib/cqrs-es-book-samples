package org.example.cqrsesbooksamples.chapter06._01;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Service;

// 削除済み（旧 6.21）
@Service
public record BankTransferService(
    BankTransferApi bankApi,
    EventGateway eventGateway
) {
    @CommandHandler
    public void handle(RequestBankTransferCommand command) {
        var transferRequest = new BankTransferRequest(
            command.accountId(),
            command.amount()
        );

        try {
            var response = bankApi.requestTransfer(transferRequest);
            eventGateway.publish(new BankTransferRequestedEvent(
                command.paymentProcessId(),
                response.getTransferId()
            ));
        } catch (BankApiException e) {
            eventGateway.publish(new BankTransferRequestFailedEvent(
                command.paymentProcessId(),
                e.getMessage()
            ));
        }
    }
}
