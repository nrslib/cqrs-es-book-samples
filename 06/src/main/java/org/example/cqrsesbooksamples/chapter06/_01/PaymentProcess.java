package org.example.cqrsesbooksamples.chapter06._01;

import java.math.BigDecimal;

public record PaymentProcess(
    PaymentProcessId paymentProcessId,
    AccountId accountId,
    PaymentProcessStatus status
) {
    public static PaymentProcessAcceptedEvent create(
            PaymentProcessId paymentProcessId,
            AccountId accountId,
            BigDecimal amount,
            PaymentMethod paymentMethod) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        return new PaymentProcessAcceptedEvent(paymentProcessId, accountId, amount, paymentMethod);
    }

    public PaymentProcessCompletedEvent complete() {
        if (status != PaymentProcessStatus.STARTED) {
            throw new IllegalStateException("Payment process is not in progress");
        }
        return new PaymentProcessCompletedEvent(paymentProcessId, accountId);
    }

    public static PaymentProcess applyEvent(PaymentProcessAcceptedEvent event) {
        return new PaymentProcess(event.paymentProcessId(), event.accountId(), PaymentProcessStatus.STARTED);
    }

    public PaymentProcess applyEvent(PaymentProcessEvent event) {
        return switch (event) {
            case PaymentProcessAcceptedEvent e -> applyEvent(e);
            case PaymentProcessCompletedEvent _ ->
                new PaymentProcess(paymentProcessId, accountId, PaymentProcessStatus.COMPLETED);
            default -> throw new IllegalStateException("Unknown event type: " + event);
        };
    }
}
