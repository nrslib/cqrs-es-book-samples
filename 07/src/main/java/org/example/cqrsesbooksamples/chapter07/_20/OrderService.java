// 紙面照合: 7.17
package org.example.cqrsesbooksamples.chapter07._20;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


// 注文システムの実装
@Service
public class OrderService {
    private final KafkaTemplate<String, PaymentRequestMessage> kafkaTemplate;

    public OrderService(KafkaTemplate<String, PaymentRequestMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void requestPayment(Order order) {
        var message = new PaymentRequestMessage(order.getId(), order.getAmount());
        kafkaTemplate.send("payment-requests", message);
    }
}
