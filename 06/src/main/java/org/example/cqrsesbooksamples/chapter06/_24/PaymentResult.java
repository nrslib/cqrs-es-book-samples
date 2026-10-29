package org.example.cqrsesbooksamples.chapter06._24;

import java.util.UUID;

public record PaymentResult(UUID processId, PaymentStatus status) {}
