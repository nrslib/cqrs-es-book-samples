package org.example.cqrsesbooksamples.chapter02._19;

public record UserDeletedEvent(
    String userId
) implements UserEvent {
}
