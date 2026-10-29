package org.example.cqrsesbooksamples.chapter07._20;

import java.math.BigDecimal;

public class Order {
    private final String id;
    private final BigDecimal amount;

    public Order(String id, BigDecimal amount) {
        this.id = id;
        this.amount = amount;
    }

    public String getId() { return id; }
    public BigDecimal getAmount() { return amount; }
}
