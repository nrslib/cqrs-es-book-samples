// 紙面照合: 6.8
package org.example.cqrsesbooksamples.chapter06._09;

import java.time.Duration;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.example.cqrsesbooksamples.chapter06._01.CreditAcceptedEvent;
import org.example.cqrsesbooksamples.chapter06._01.CreditApi;
import org.example.cqrsesbooksamples.chapter06._01.ExecuteCreditPaymentCommand;
import org.example.cqrsesbooksamples.chapter06._01.CreditMakePaymentFailedEvent;
import org.example.cqrsesbooksamples.chapter06._01.CreditMakePaymentRequest;
import org.example.cqrsesbooksamples.chapter06._01.CreditMakePaymentResponse;
import org.example.cqrsesbooksamples.chapter06._01.CreditRejectedEvent;
import org.springframework.stereotype.Service;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.control.Try;


@Service
public class CreditService {

    private final CreditApi creditApi;
    private final QueryGateway queryGateway;
    private final EventGateway eventGateway;
    private final Retry retry;

    public CreditService(CreditApi creditApi, QueryGateway queryGateway, EventGateway eventGateway) {
        this.creditApi = creditApi;
        this.queryGateway = queryGateway;
        this.eventGateway = eventGateway;

        this.retry = Retry.of("creditRetry", RetryConfig.<CreditMakePaymentResponse>custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(5))
            .retryOnResult(response -> response.statusCode() != 200)  // 非200応答も再試行の対象にする
            .build());
    }

    @CommandHandler
    public void handle(ExecuteCreditPaymentCommand command) {
        var result = Try.of(() -> Retry.decorateSupplier(retry,
            () -> creditApi.makePayment(new CreditMakePaymentRequest(
                command.paymentProcessId(), command.accountId(), command.amount()))
        ).get());

        if (result.isFailure()) {
            // 接続エラー等で再試行を使い切った場合も失敗イベントを発行する
            eventGateway.publish(new CreditMakePaymentFailedEvent(command, result.getCause().getMessage()));
            return;
        }

        var response = result.get();
        if (response.statusCode() != 200) {
            // 再試行を使い切ってもHTTPが失敗のままなら、基本パターンと同じ扱いで失敗イベントを発行する
            eventGateway.publish(new CreditMakePaymentFailedEvent(command, "HTTP " + response.statusCode()));
            return;
        }

        if (response.accepted()) {
            eventGateway.publish(new CreditAcceptedEvent(command.paymentProcessId()));
        } else {
            eventGateway.publish(new CreditRejectedEvent(command.paymentProcessId()));
        }
    }
}
