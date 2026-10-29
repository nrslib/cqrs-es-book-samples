// 紙面照合: 6.19
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


@Tag(description = "Payment API", name = "Payment API")
@RestController
@RequestMapping("/api/payments")
public record PaymentController(
   CommandGateway commandGateway,
   QueryGateway queryGateway
) {
   private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

   @Operation(summary = "Request payment with sync response.")
   @PostMapping("/sync")
   public Mono<PaymentResult> requestPaymentSync(@RequestBody PaymentRequest request) {
       var processId = UUID.randomUUID();
       logger.info("Payment Process ID: {}", processId);

       // 結果を受け取るためのSubscriptionQueryを準備
       var query = new PaymentStatusQuery(processId);
       var result = queryGateway.subscriptionQuery(
           query,
           ResponseTypes.instanceOf(PaymentStatus.class),
           ResponseTypes.instanceOf(PaymentStatus.class)
       );

       // 支払い処理の開始（同期入口では呼び出し元が採番したプロセスIDを渡す）
       var send = Mono.fromFuture(() -> commandGateway.send(new StartPaymentCommand(
           processId,
           request.accountId(),
           request.amount(),
           request.method()
       )));

       // コマンド送信の完了後、ノンブロッキングに結果を待機
       return send.then(Mono.from(result.updates()
               .filter(status -> status.isCompleted() || status.isFailed())
               .next()
               .map(status -> new PaymentResult(processId, status))
               .timeout(Duration.ofSeconds(30))
       )).doFinally(signalType -> result.close());
   }
}
