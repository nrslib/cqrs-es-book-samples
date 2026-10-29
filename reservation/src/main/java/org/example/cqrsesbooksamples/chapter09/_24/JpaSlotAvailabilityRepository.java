package org.example.cqrsesbooksamples.chapter09._24;

import org.example.cqrsesbooksamples.chapter09._01.projection.SlotAvailability;
import org.springframework.stereotype.Repository;

@Repository
public class JpaSlotAvailabilityRepository implements SlotAvailabilityRepository {

    private final org.example.cqrsesbooksamples.chapter09._01.projection.SlotAvailabilityRepository repository;

    public JpaSlotAvailabilityRepository(
            org.example.cqrsesbooksamples.chapter09._01.projection.SlotAvailabilityRepository repository) {
        this.repository = repository;
    }

    @Override
    public long countClaimed(String slotId) {
        var availability = get(slotId);
        return availability.getCapacity() - availability.getRemaining();
    }

    @Override
    public long getCapacity(String slotId) {
        return get(slotId).getCapacity();
    }

    private SlotAvailability get(String slotId) {
        return repository.findById(slotId)
            .orElseThrow(() -> new IllegalStateException("予約枠が見つかりません: " + slotId));
    }
}
