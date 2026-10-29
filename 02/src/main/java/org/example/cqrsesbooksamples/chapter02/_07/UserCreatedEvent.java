package org.example.cqrsesbooksamples.chapter02._7;

public record UserCreatedEvent(
    String userId,
    String name,
    String email
) {}
