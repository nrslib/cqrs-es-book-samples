package org.example.cqrsesbooksamples.chapter03._66;

public interface ProcessedEventRepository {
    boolean existsByEventId(String eventId);

    ProcessedEvent save(ProcessedEvent processedEvent);
}
