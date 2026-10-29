package org.example.cqrsesbooksamples.chapter03._67;

import java.math.BigDecimal;

public class OrderPlacedEvent {
    private final String orderId;
    private final String customerEmail;
    private final BigDecimal amount;

    public OrderPlacedEvent(String orderId, String customerEmail, BigDecimal amount) {
        this.orderId = orderId;
        this.customerEmail = customerEmail;
        this.amount = amount;
    }

    public String orderId() { return orderId; }
    public String customerEmail() { return customerEmail; }
    public BigDecimal amount() { return amount; }
}
