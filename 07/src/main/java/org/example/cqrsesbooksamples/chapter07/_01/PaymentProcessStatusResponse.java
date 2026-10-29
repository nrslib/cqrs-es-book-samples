package org.example.cqrsesbooksamples.chapter07._01;

import java.time.LocalDateTime;

public record PaymentProcessStatusResponse(
    String processId,
    PaymentProcessState currentState,
    LocalDateTime lastUpdated
) {}
