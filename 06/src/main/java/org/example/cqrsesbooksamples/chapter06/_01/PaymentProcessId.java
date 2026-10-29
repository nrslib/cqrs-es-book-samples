package org.example.cqrsesbooksamples.chapter06._01;

import java.util.UUID;

public record PaymentProcessId(String value) {
    public PaymentProcessId() {
        this(UUID.randomUUID().toString());
    }

    public String asString() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
