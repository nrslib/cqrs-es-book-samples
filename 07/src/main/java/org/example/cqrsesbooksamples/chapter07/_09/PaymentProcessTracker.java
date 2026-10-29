package org.example.cqrsesbooksamples.chapter07._09;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment_process_tracker")
public class PaymentProcessTracker {
    @Id
    private String processId;
    private String currentStatus;
    private String lastEventId;
    private Instant lastUpdated;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<TrackedEvent> events = new ArrayList<>();

    public String getProcessId() { return processId; }
    public String getCurrentStatus() { return currentStatus; }
    public String getLastEventId() { return lastEventId; }
    public Instant getLastUpdated() { return lastUpdated; }
    public List<TrackedEvent> getEvents() { return events; }

    public void setProcessId(String processId) { this.processId = processId; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
    public void setLastEventId(String lastEventId) { this.lastEventId = lastEventId; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
}
