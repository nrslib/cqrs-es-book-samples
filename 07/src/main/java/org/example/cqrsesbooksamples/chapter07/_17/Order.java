package org.example.cqrsesbooksamples.chapter07._17;

import java.math.BigDecimal;

public class Order {
    private final String orderId;
    private final BigDecimal totalAmount;
    private final String paymentMethod;

    public Order(String orderId, BigDecimal totalAmount, String paymentMethod) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
    }

    public String getOrderId() { return orderId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
}
