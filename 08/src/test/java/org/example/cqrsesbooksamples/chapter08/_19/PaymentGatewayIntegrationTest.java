// 紙面照合: 8.19, 8.20
package org.example.cqrsesbooksamples.chapter08._19;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.net.ConnectException;

import org.example.cqrsesbooksamples.chapter08._18.PaymentGateway;
import org.example.cqrsesbooksamples.chapter08._18.PaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.timeout.ReadTimeoutException;
import reactor.core.publisher.Mono;


public class PaymentGatewayIntegrationTest {
    private PaymentGateway paymentGateway;
    private WebClient webClient;

    @BeforeEach
    void setUp() {
        webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        paymentGateway = new PaymentGateway(webClient);
    }


    @Test
    void shouldHandleSuccessfulPayment() {
        var responseBody = """
            {
                "status": "success",
                "transactionId": "TX123"
            }
            """;
        mockWebClientResponse(Mono.just(responseBody));

        var result = paymentGateway.processPayment(
            new PaymentRequest("ORDER-001", BigDecimal.valueOf(1000))
        );

        assertTrue(result.isSuccessful());
        assertEquals("TX123", result.getTransactionId());
    }


    @Test
    void shouldHandleNetworkError() {
        failConnection(new ConnectException("Connection refused"));

        var result = paymentGateway.processPayment(
            new PaymentRequest("ORDER-001", BigDecimal.valueOf(1000))
        );

        assertFalse(result.isSuccessful());
        assertEquals("CONNECTION_ERROR", result.getErrorCode());
    }


    @Test
    void shouldHandleTimeout() {
        failConnection(ReadTimeoutException.INSTANCE);

        var result = paymentGateway.processPayment(
            new PaymentRequest("ORDER-001", BigDecimal.valueOf(1000))
        );

        assertFalse(result.isSuccessful());
        assertEquals("TIMEOUT", result.getErrorCode());
    }


    private void mockWebClientResponse(Mono<String> response) {
        when(webClient.post().uri(anyString()).bodyValue(any()).retrieve().bodyToMono(String.class))
            .thenReturn(response);
    }


    private void failConnection(Throwable cause) {
        var client = WebClient.builder()
            .baseUrl("http://payment-service")
            .clientConnector((method, uri, callback) -> Mono.error(cause))
            .build();
        paymentGateway = new PaymentGateway(client);
    }
}
