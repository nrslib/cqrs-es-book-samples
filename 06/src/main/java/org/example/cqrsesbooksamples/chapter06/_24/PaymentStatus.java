package org.example.cqrsesbooksamples.chapter06._24;

public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED;

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isFailed() {
        return this == FAILED;
    }
}
