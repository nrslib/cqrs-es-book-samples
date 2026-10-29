package org.example.cqrsesbooksamples.chapter02._54;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class OrderReadModel {
    @Id
    private String orderId;
    private int amount;

    protected OrderReadModel() {}

    public OrderReadModel(String orderId, int amount) {
        this.orderId = orderId;
        this.amount = amount;
    }

    public String getOrderId() { return orderId; }
    public int getAmount() { return amount; }
}
