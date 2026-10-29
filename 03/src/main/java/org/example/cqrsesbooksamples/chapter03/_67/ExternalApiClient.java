package org.example.cqrsesbooksamples.chapter03._67;

import java.math.BigDecimal;

public interface ExternalApiClient {
    void notifyOrder(String idempotencyKey, String customerEmail, BigDecimal amount);
}
