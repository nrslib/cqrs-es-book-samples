package org.example.cqrsesbooksamples.chapter03._07;

public class OrderPlacedEvent {
    private final String orderId;
    private final String productId;

    public OrderPlacedEvent(String orderId, String productId) {
        this.orderId = orderId;
        this.productId = productId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProductId() {
        return productId;
    }
}
