package org.example.cqrsesbooksamples.chapter08._18;

import java.math.BigDecimal;

public class PaymentRequest {
    private final String orderReference;
    private final BigDecimal amount;

    public PaymentRequest(String orderReference, BigDecimal amount) {
        this.orderReference = orderReference;
        this.amount = amount;
    }

    public String getOrderReference() {
        return orderReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
