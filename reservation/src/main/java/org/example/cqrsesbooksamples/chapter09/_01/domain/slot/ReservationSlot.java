// 紙面照合: 9.6

package org.example.cqrsesbooksamples.chapter09._01.domain.slot;

import org.example.cqrsesbooksamples.chapter09._01.event.*;

import java.time.LocalDate;
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

    public int remaining() {
        return capacity - claimedReservations.size();
    }

    public static ReservationSlotCreatedEvent create(String slotId, LocalDate slotDate, TimeRange timeRange, int capacity) {
        if (slotDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("予約枠の日付は今日以降である必要があります");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("定員は1以上である必要があります");
        }
        return new ReservationSlotCreatedEvent(slotId, slotDate, timeRange.startTime(), timeRange.endTime(), capacity);
    }

    public static ReservationSlot from(ReservationSlotCreatedEvent event) {
        return new ReservationSlot(event.slotId(), event.capacity());
    }

    public Optional<ReservationSlotEvent> claimSlot(String reservationId) {
        if (claimedReservations.contains(reservationId)) {
            // 確保中の予約からの再指示は何も起こさない
            return Optional.empty();
        }
        if (remaining() <= 0) {
            return Optional.of(new SlotClaimFailedEvent(slotId, reservationId));
        }
        return Optional.of(new SlotClaimedEvent(slotId, reservationId));
    }

    public Optional<SlotReleasedEvent> releaseSlot(String reservationId) {
        if (!claimedReservations.contains(reservationId)) {
            // 確保していない予約の解放は何も起こさない
            return Optional.empty();
        }
        return Optional.of(new SlotReleasedEvent(slotId, reservationId));
    }

    public ReservationSlot applyEvent(ReservationSlotEvent event) {
        return switch (event) {
            case ReservationSlotCreatedEvent e -> from(e);
            case SlotClaimedEvent e -> {
                var newSet = new HashSet<>(claimedReservations);
                newSet.add(e.reservationId());
                yield new ReservationSlot(slotId, capacity, Set.copyOf(newSet));
            }
            case SlotReleasedEvent e -> {
                var newClaimed = new HashSet<>(claimedReservations);
                newClaimed.remove(e.reservationId());
                yield new ReservationSlot(slotId, capacity, Set.copyOf(newClaimed));
            }
            case SlotClaimFailedEvent e -> {
                // 失敗は記録するだけで状態を変えない
                yield this;
            }
        };
    }
}
