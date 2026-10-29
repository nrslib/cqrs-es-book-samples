package org.example.cqrsesbooksamples.chapter02._75;

public record UserRegisteredEvent(
    String userId,
    String name,
    String email
) {}
