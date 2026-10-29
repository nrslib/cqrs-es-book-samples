package org.example.cqrsesbooksamples.chapter07.support;

import java.math.BigDecimal;
import java.util.UUID;

// processId は追跡用に呼び出し側で採番して渡す（省略時は集約側で採番）
public record StartPaymentCommand(UUID processId, UUID accountId, BigDecimal amount, PaymentMethod paymentMethod) {
    public StartPaymentCommand(UUID accountId, BigDecimal amount, PaymentMethod paymentMethod) {
        this(null, accountId, amount, paymentMethod);
    }
}
