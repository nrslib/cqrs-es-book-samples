package org.example.cqrsesbooksamples.chapter06._24;

import java.math.BigDecimal;
import java.util.UUID;

import org.example.cqrsesbooksamples.chapter06._01.PaymentMethod;

public record PaymentRequest(UUID accountId, BigDecimal amount, PaymentMethod method) {}
