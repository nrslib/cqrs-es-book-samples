package org.example.cqrsesbooksamples.chapter08._21;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_status_08")
public class OrderStatusEntry {
    @Id
    private String orderId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    protected OrderStatusEntry() {}

    public OrderStatusEntry(String orderId, OrderStatus status) {
        this.orderId = orderId;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
