package org.example.cqrsesbooksamples.chapter03._63;

public record UserRegisteredEvent(
    String userId,
    String name,
    String email
) {}
