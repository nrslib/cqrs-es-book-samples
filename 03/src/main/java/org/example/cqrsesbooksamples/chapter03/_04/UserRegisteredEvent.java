package org.example.cqrsesbooksamples.chapter03._04;

import java.time.Instant;

public record UserRegisteredEvent(
    String userId,
    String name,
    String fullName,
    String email,
    Instant registeredAt
) {}
