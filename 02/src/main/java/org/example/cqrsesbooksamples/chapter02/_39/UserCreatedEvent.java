package org.example.cqrsesbooksamples.chapter02._39;

public record UserCreatedEvent(
    String userId,
    String name,
    String email
) implements UserEvent {
}
