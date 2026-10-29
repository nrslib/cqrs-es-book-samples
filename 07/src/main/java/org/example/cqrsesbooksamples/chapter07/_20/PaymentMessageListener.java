// 紙面照合: 7.17
package org.example.cqrsesbooksamples.chapter07._20;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


// 決済システムの実装
@Component
public class PaymentMessageListener {
    @KafkaListener(topics = "payment-requests")
    public void handlePaymentRequest(PaymentRequestMessage message) {
        // 支払い処理の実行
    }
}
