package org.example.cqrsesbooksamples.chapter02._72;

public record UserRegisteredEvent(
    String userId,
    String firstName,
    String lastName,
    String email
) {}
