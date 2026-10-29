// 紙面照合: 6.6
package org.example.cqrsesbooksamples.chapter06._01;

import java.math.BigDecimal;


public record ExecuteCreditPaymentCommand(PaymentProcessId paymentProcessId, AccountId accountId, BigDecimal amount, int count) {
    public ExecuteCreditPaymentCommand(PaymentProcessId paymentProcessId, AccountId accountId, BigDecimal amount) {
        this(paymentProcessId, accountId, amount, 0);
    }
    public ExecuteCreditPaymentCommand countUp() {
        return new ExecuteCreditPaymentCommand(paymentProcessId, accountId, amount, count + 1);
    }
}
