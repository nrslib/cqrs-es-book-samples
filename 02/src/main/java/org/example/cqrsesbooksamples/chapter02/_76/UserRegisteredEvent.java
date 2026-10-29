package org.example.cqrsesbooksamples.chapter02._76;

public record UserRegisteredEvent(
    String userId,
    String name,
    String email
) {}
