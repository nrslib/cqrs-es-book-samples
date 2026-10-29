package org.example.cqrsesbooksamples.chapter03._64;

import java.math.BigDecimal;

public class OrderPlacedEvent {
    private final String eventId;
    private final String orderId;
    private final String orderMonth;
    private final BigDecimal amount;
    private final String customerEmail;

    public OrderPlacedEvent(String eventId, String orderId, String orderMonth,
                            BigDecimal amount, String customerEmail) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.orderMonth = orderMonth;
        this.amount = amount;
        this.customerEmail = customerEmail;
    }

    public String eventId() { return eventId; }
    public String orderId() { return orderId; }
    public String orderMonth() { return orderMonth; }
    public BigDecimal amount() { return amount; }
    public String customerEmail() { return customerEmail; }
}
