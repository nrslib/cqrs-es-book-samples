package org.example.cqrsesbooksamples.chapter07.support;

import java.util.UUID;

public record PaymentProcessId(String value) {
    public PaymentProcessId() {
        this(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
