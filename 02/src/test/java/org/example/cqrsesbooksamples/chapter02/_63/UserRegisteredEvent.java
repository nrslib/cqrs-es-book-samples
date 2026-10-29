package org.example.cqrsesbooksamples.chapter02._63;

public record UserRegisteredEvent(
    String userId,
    String name,
    String email
) {}
