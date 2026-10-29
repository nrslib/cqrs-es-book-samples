package org.example.cqrsesbooksamples.chapter08._18;

import java.math.BigDecimal;

import org.axonframework.commandhandling.CommandHandler;

/**
 * 内側のコマンドを外部APIのリクエスト構造へ翻訳する腐敗防止層。
 */
public class PaymentServiceAdapter {
    private final PaymentGateway paymentGateway;

    public PaymentServiceAdapter(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    @CommandHandler
    public PaymentResult handle(ProcessPaymentCommand command) {
        var request = new PaymentRequest(
            command.orderId(),
            BigDecimal.valueOf(command.amount())
        );
        return paymentGateway.processPayment(request);
    }
}
