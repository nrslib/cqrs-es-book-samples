package org.example.cqrsesbooksamples.chapter07._16;

public class OrderCreatedEvent {
    private final String orderId;
    private final OrderType orderType;

    public OrderCreatedEvent(String orderId, OrderType orderType) {
        this.orderId = orderId;
        this.orderType = orderType;
    }

    public String getOrderId() {
        return orderId;
    }

    public OrderType getOrderType() {
        return orderType;
    }
}
