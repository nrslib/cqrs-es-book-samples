package org.example.cqrsesbooksamples.chapter02._45;

public record UserUpdatedEvent(
    String userId,
    String name,
    String email
) implements UserEvent {
}
