package org.example.cqrsesbooksamples.chapter08._18;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.timeout.ReadTimeoutException;

/**
 * 決済事業者APIのクライアント。
 * レスポンスJSONと物理層の例外を、内側の PaymentResult へ翻訳する。
 */
public class PaymentGateway {
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentGateway(WebClient webClient) {
        this.webClient = webClient;
    }

    public PaymentResult processPayment(PaymentRequest request) {
        try {
            String body = webClient.post()
                .uri("/process")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            var json = objectMapper.readTree(body);
            var status = json.get("status").asText();
            var transactionId = json.has("transactionId") ? json.get("transactionId").asText() : null;
            return new PaymentResult(status, transactionId);
        } catch (WebClientRequestException e) {
            return PaymentResult.failure(e.getCause() instanceof ReadTimeoutException
                ? "TIMEOUT" : "CONNECTION_ERROR");
        } catch (ReadTimeoutException e) {
            return PaymentResult.failure("TIMEOUT");
        } catch (Exception e) {
            return PaymentResult.failure("UNEXPECTED_ERROR");
        }
    }
}
