package org.example.cqrsesbooksamples.chapter02._73;

public record UserRegisteredEvent(
    String userId,
    String name,
    String email
) {}
