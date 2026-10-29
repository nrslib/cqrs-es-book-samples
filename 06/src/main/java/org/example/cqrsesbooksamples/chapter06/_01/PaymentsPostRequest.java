// 紙面照合: 6.1
package org.example.cqrsesbooksamples.chapter06._01;

import java.math.BigDecimal;
import java.util.UUID;


public record PaymentsPostRequest(UUID accountId, BigDecimal amount, PaymentMethod paymentMethod) {}
