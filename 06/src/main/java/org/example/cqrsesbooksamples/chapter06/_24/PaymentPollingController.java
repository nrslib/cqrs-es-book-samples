// 紙面照合: 6.20
package org.example.cqrsesbooksamples.chapter06._24;

import java.time.Duration;
import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.example.cqrsesbooksamples.chapter06._01.StartPaymentCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;


@Tag(description = "Payment API", name = "Payment API")
@RestController
@RequestMapping("/api/payments")
public record PaymentPollingController(
    CommandGateway commandGateway,
    QueryGateway queryGateway
) {
    private static final Logger logger = LoggerFactory.getLogger(PaymentPollingController.class);

    @Operation(summary = "Request payment with sync response (polling-based).")
    @PostMapping("/sync-polling")
    public Mono<PaymentResult> requestPaymentSyncPolling(@RequestBody PaymentRequest request) {
        var processId = UUID.randomUUID();
        logger.info("Payment Process ID (polling): {}", processId);

        return Mono.fromFuture(() -> commandGateway.send(new StartPaymentCommand(
                processId, request.accountId(), request.amount(), request.method())))
            .then(Mono.fromFuture(() -> queryGateway.query(
                    new PaymentStatusQuery(processId),
                    ResponseTypes.instanceOf(PaymentStatus.class)))
                .map(status -> {
                    if (status.isCompleted() || status.isFailed()) {
                        return new PaymentResult(processId, status);
                    }
                    throw new IllegalStateException("not yet");
                })
                .retryWhen(Retry.fixedDelay(60, Duration.ofMillis(500)))
                .timeout(Duration.ofSeconds(30)));
    }
}
