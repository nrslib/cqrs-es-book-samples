package org.example.cqrsesbooksamples.chapter03._65;

public record UserRegisteredEvent(
    String userId,
    String name,
    String email
) {}
