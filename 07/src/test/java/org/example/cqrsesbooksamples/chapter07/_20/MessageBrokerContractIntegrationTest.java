// 検証補助: 7.17
package org.example.cqrsesbooksamples.chapter07._20;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/**
 * メッセージブローカーを介した契約（リスト7.17）の検証。
 * 組み込みKafkaブローカーを経由して、注文システム側の送信が
 * 決済システム側の契約メッセージ（PaymentRequestMessage）として届くことを確かめる。
 */
@SpringBootTest(
        classes = Chapter07_20TestApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:chapter07_20;DB_CLOSE_DELAY=-1",
                "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
                "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
                "spring.kafka.consumer.properties.spring.json.trusted.packages=org.example.cqrsesbooksamples.chapter07._20",
                "spring.kafka.consumer.group-id=chapter07-20",
                "spring.kafka.consumer.auto-offset-reset=earliest"
        })
@EmbeddedKafka(partitions = 1, topics = "payment-requests",
        bootstrapServersProperty = "spring.kafka.bootstrap-servers")
class MessageBrokerContractIntegrationTest {

    @Autowired
    private OrderService orderService;

    @MockitoSpyBean
    private PaymentMessageListener paymentMessageListener;

    @Test
    void 注文システムの送信が決済システムの契約メッセージとして届く() {
        orderService.requestPayment(new Order("order-1", new BigDecimal("1200")));

        // 非同期配送のため、リスナーが契約どおりのメッセージを受け取るまで待つ
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                verify(paymentMessageListener).handlePaymentRequest(
                        new PaymentRequestMessage("order-1", new BigDecimal("1200"))));
    }
}
