package org.example.cqrsesbooksamples.chapter02._73;

public record UserUpdatedEvent(
    String userId,
    String name,
    String email
) {}
