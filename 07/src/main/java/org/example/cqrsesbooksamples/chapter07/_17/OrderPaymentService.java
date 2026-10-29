// 紙面照合: 7.15
package org.example.cqrsesbooksamples.chapter07._17;

import org.springframework.stereotype.Service;

// 良い例：注文システムが決済システムのAPIを利用する
@Service
public class OrderPaymentService {
    private final PaymentClient paymentClient;

    public OrderPaymentService(PaymentClient paymentClient) {
        this.paymentClient = paymentClient;
    }

    public PaymentResult processPayment(Order order) {
        var request = new PaymentRequest(
            order.getOrderId(),
            order.getTotalAmount(),
            order.getPaymentMethod()
        );
        return paymentClient.requestPayment(request);
    }
}
