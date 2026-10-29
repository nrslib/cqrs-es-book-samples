package org.example.cqrsesbooksamples.chapter07.support;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentsPostRequest(UUID accountId, BigDecimal amount, PaymentMethod paymentMethod) {}
