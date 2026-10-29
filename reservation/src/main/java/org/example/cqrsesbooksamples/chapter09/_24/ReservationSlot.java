package org.example.cqrsesbooksamples.chapter09._24;

import org.example.cqrsesbooksamples.chapter09._01.event.SlotClaimedEvent;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public record ReservationSlot(
    String slotId,
    int capacity,
    Set<String> claimedReservations
) {
    public ReservationSlot(String slotId, int capacity) {
        this(slotId, capacity, Set.of());
    }

    // 削除済み（旧 9.24）
    public Optional<SlotClaimedEvent> claimSlot(String reservationId) {
        if (claimedReservations.contains(reservationId)) {
            return Optional.empty();
        }
        // 定員チェックなし — オーバーブッキングを許容
        return Optional.of(new SlotClaimedEvent(slotId, reservationId));
    }

    public ReservationSlot applyEvent(SlotClaimedEvent event) {
        var newSet = new HashSet<>(claimedReservations);
        newSet.add(event.reservationId());
        return new ReservationSlot(slotId, capacity, Set.copyOf(newSet));
    }
}
