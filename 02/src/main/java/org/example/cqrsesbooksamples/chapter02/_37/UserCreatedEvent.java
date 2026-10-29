package org.example.cqrsesbooksamples.chapter02._37;

public record UserCreatedEvent(
    String userId,
    String name,
    String email
) {}
