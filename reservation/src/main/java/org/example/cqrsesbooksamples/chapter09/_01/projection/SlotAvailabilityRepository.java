package org.example.cqrsesbooksamples.chapter09._01.projection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SlotAvailabilityRepository extends JpaRepository<SlotAvailability, String> {
    @Modifying
    @Query("UPDATE SlotAvailability s SET s.remaining = s.remaining - 1 WHERE s.slotId = :slotId")
    void decrementRemaining(String slotId);

    @Modifying
    @Query("UPDATE SlotAvailability s SET s.remaining = s.remaining + 1 WHERE s.slotId = :slotId")
    void incrementRemaining(String slotId);
}
