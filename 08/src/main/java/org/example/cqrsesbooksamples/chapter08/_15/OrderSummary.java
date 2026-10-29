package org.example.cqrsesbooksamples.chapter08._15;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_summary_08")
public class OrderSummary {
    @Id
    private String orderId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private int amount;
    private boolean paymentCompleted;

    protected OrderSummary() {}

    public OrderSummary(String orderId) {
        this.orderId = orderId;
        this.status = OrderStatus.CREATED;
    }

    public String getOrderId() { return orderId; }
    public OrderStatus getStatus() { return status; }
    public int getAmount() { return amount; }
    public boolean isPaymentCompleted() { return paymentCompleted; }

    public void setStatus(OrderStatus status) { this.status = status; }
    public void setAmount(int amount) { this.amount = amount; }
    public void setPaymentCompleted(boolean paymentCompleted) { this.paymentCompleted = paymentCompleted; }
}
