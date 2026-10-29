package org.example.cqrsesbooksamples.chapter07._09;

import java.time.Instant;

import jakarta.persistence.Embeddable;

@Embeddable
public class TrackedEvent {
    private String eventType;
    private String eventId;
    private Instant occurredAt;

    protected TrackedEvent() {}

    public TrackedEvent(String eventType, String eventId, Instant occurredAt) {
        this.eventType = eventType;
        this.eventId = eventId;
        this.occurredAt = occurredAt;
    }

    public String getEventType() { return eventType; }
    public String getEventId() { return eventId; }
    public Instant getOccurredAt() { return occurredAt; }
}
