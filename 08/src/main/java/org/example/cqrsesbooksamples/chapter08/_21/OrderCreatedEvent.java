package org.example.cqrsesbooksamples.chapter08._21;

public record OrderCreatedEvent(String orderId, int amount) {
    public String getOrderId() {
        return orderId;
    }

    public int getAmount() {
        return amount;
    }
}
