// 紙面照合: 6.2
package org.example.cqrsesbooksamples.chapter06._01;

import java.math.BigDecimal;
import java.util.UUID;


// processId は呼び出し元が採番して渡せる（省略時は集約が採番）
public record StartPaymentCommand(UUID processId, UUID accountId, BigDecimal amount, PaymentMethod paymentMethod) {
    public StartPaymentCommand(UUID accountId, BigDecimal amount, PaymentMethod paymentMethod) {
        this(null, accountId, amount, paymentMethod);
    }
}
