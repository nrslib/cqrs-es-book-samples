// 検証補助: 7.16
package org.example.cqrsesbooksamples.chapter07._19;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

/**
 * 決済システムが定義するREST APIの契約（リスト7.16）の検証。
 * 決済側が自分の境界に置いた契約（PaymentRequest / PaymentResponse）で
 * 要求を受け付け、受理応答を返すことを確かめる。
 */
class PaymentControllerTest {

    @Test
    void 契約に沿った要求を受理する() {
        var controller = new PaymentController();

        var response = controller.createPayment(
                new PaymentRequest("order-1", new BigDecimal("1200"), "CREDIT"));

        assertThat(response.paymentId()).isNotBlank();
        assertThat(response.status()).isEqualTo("ACCEPTED");
    }
}
