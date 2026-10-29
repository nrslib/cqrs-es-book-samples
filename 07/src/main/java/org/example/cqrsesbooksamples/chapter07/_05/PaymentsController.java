// 紙面照合: 7.4
package org.example.cqrsesbooksamples.chapter07._05;

import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.MetaData;
import org.axonframework.queryhandling.QueryGateway;
import org.example.cqrsesbooksamples.chapter07.support.GetAccountQuery;
import org.example.cqrsesbooksamples.chapter07.support.GetAccountQueryResult;
import org.example.cqrsesbooksamples.chapter07.support.NotFoundException;
import org.example.cqrsesbooksamples.chapter07.support.PaymentProcessId;
import org.example.cqrsesbooksamples.chapter07.support.PaymentsPostRequest;
import org.example.cqrsesbooksamples.chapter07.support.PaymentsPostResponse;
import org.example.cqrsesbooksamples.chapter07.support.StartPaymentCommand;
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
        // 独自のプロセスIDを採番し、コマンドとメタデータの両方に載せる
        var processId = UUID.randomUUID();
        var metadata = MetaData.with("processId", processId.toString());
        commandGateway.sendAndWait(
            new StartPaymentCommand(processId, request.accountId(), request.amount(), request.paymentMethod()),
            metadata);
        return new PaymentsPostResponse(processId.toString());
    }
}
