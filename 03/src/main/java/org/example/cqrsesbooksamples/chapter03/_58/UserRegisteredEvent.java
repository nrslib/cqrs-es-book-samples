package org.example.cqrsesbooksamples.chapter03._58;

public record UserRegisteredEvent(
    String userId,
    String name,
    String email
) {}
