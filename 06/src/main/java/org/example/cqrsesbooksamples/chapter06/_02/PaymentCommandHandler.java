package org.example.cqrsesbooksamples.chapter06._02;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.example.cqrsesbooksamples.chapter06._01.GetAccountQuery;
import org.example.cqrsesbooksamples.chapter06._01.GetAccountQueryResult;
import org.example.cqrsesbooksamples.chapter06._01.NotFoundException;
import org.example.cqrsesbooksamples.chapter06._01.PaymentProcessId;
import org.example.cqrsesbooksamples.chapter06._01.StartPaymentCommand;
import org.springframework.stereotype.Component;

// 削除済み（旧 6.2）
@Component
public class PaymentCommandHandler {
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    public PaymentCommandHandler(CommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @CommandHandler
    public PaymentProcessId handle(RequestPaymentCommand command) {
        var query = new GetAccountQuery(command.accountId());
        var getAccountResult = queryGateway.query(query, GetAccountQueryResult.class).join();

        if (getAccountResult.accountData().isEmpty()) {
            throw new NotFoundException("Account not found");
        }

        return commandGateway.sendAndWait(new StartPaymentCommand(
            command.accountId(),
            command.amount(),
            command.paymentMethod()
        ));
    }
}
