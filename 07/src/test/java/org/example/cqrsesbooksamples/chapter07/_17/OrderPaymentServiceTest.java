// 検証補助: 7.15
package org.example.cqrsesbooksamples.chapter07._17;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OrderPaymentServiceTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void 注文情報を決済の契約に変換し決済結果を返す(boolean successful) {
        var client = mock(PaymentClient.class);
        var service = new OrderPaymentService(client);
        var request = new PaymentRequest("order-1", new BigDecimal("9800"), "CREDIT");
        var expected = new PaymentResult("order-1", successful);
        when(client.requestPayment(request)).thenReturn(expected);

        var result = service.processPayment(new Order("order-1", new BigDecimal("9800"), "CREDIT"));

        assertEquals(expected, result);
        verify(client).requestPayment(request);
    }

    @Test
    void クライアントの例外を握り潰さず呼び出し元へ伝える() {
        var client = mock(PaymentClient.class);
        var service = new OrderPaymentService(client);
        var request = new PaymentRequest("order-1", new BigDecimal("9800"), "CREDIT");
        var failure = new IllegalStateException("決済結果を取得できませんでした");
        when(client.requestPayment(request)).thenThrow(failure);

        var thrown = assertThrows(IllegalStateException.class,
            () -> service.processPayment(new Order("order-1", new BigDecimal("9800"), "CREDIT")));

        assertSame(failure, thrown);
    }
}
