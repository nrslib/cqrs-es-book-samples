// 紙面照合: 6.1
package org.example.cqrsesbooksamples.chapter06._01;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(description = "Payment API", name = "Payment API")
@RestController
@RequestMapping("/api/payments")
public record PaymentsController(CommandGateway commandGateway, QueryGateway queryGateway) {
    @Operation(summary = "Request payment.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentsPostResponse post(@RequestBody PaymentsPostRequest request) {
        var query = new GetAccountQuery(request.accountId());
        var getAccountResult = queryGateway.query(query, GetAccountQueryResult.class).join();
        if (getAccountResult.accountData().isEmpty()) {
            throw new NotFoundException("Account not found");
        }
        PaymentProcessId paymentProcessId = commandGateway.sendAndWait(new StartPaymentCommand(request.accountId(), request.amount(), request.paymentMethod()));
        return new PaymentsPostResponse(paymentProcessId.value());
    }
}
