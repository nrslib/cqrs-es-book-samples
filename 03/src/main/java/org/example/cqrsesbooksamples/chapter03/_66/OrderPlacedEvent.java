package org.example.cqrsesbooksamples.chapter03._66;

import java.math.BigDecimal;

public class OrderPlacedEvent {
    private final String eventId;
    private final String orderId;
    private final String orderMonth;
    private final BigDecimal amount;

    public OrderPlacedEvent(String eventId, String orderId, String orderMonth, BigDecimal amount) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.orderMonth = orderMonth;
        this.amount = amount;
    }

    public String eventId() { return eventId; }
    public String orderId() { return orderId; }
    public String orderMonth() { return orderMonth; }
    public BigDecimal amount() { return amount; }
}
