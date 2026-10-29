package org.example.cqrsesbooksamples.chapter03._64;

public interface OrderSummaryRepository {
    OrderSummary findByMonth(String month);

    OrderSummary save(OrderSummary summary);
}
