package org.example.cqrsesbooksamples.chapter08._21;

public record OrderCompletedEvent(String orderId) {
    public String getOrderId() {
        return orderId;
    }
}
