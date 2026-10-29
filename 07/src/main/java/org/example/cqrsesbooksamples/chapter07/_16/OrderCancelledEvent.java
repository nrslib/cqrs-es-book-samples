package org.example.cqrsesbooksamples.chapter07._16;

public class OrderCancelledEvent {
    private final String orderId;

    public OrderCancelledEvent(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }
}
