package org.example.cqrsesbooksamples.chapter03._66;

import java.math.BigDecimal;

public class OrderSummary {
    private final String month;
    private int orderCount;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public OrderSummary(String month) {
        this.month = month;
    }

    public void incrementOrderCount() {
        orderCount++;
    }

    public void addAmount(BigDecimal amount) {
        totalAmount = totalAmount.add(amount);
    }

    public String getMonth() { return month; }
    public int getOrderCount() { return orderCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}
