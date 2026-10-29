package org.example.cqrsesbooksamples.chapter02._9;

public record UserCreatedEvent(
    String userId,
    String name,
    String email
) {}
