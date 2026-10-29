// 紙面照合: 8.18
package org.example.cqrsesbooksamples.chapter08._18;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class PaymentServiceAdapterTest {
    private PaymentServiceAdapter adapter;
    private PaymentGateway paymentGateway;

    @BeforeEach
    void setUp() {
        paymentGateway = mock(PaymentGateway.class);
        adapter = new PaymentServiceAdapter(paymentGateway);
    }

    @Test
    void shouldTranslateToExternalFormat() {
        when(paymentGateway.processPayment(any()))
            .thenReturn(new PaymentResult("SUCCESS"));

        var command = new ProcessPaymentCommand("ORDER-001", 1000);
        var result = adapter.handle(command);

        verify(paymentGateway).processPayment(argThat(request ->
            request.getOrderReference().equals("ORDER-001") &&
            request.getAmount().equals(BigDecimal.valueOf(1000))
        ));
        assertTrue(result.isSuccessful());
    }
}
