// 削除済み（旧 9.7）
package org.example.cqrsesbooksamples.chapter09._01.event;

public sealed interface ReservationSlotEvent permits
        ReservationSlotCreatedEvent,
        SlotClaimedEvent,
        SlotClaimFailedEvent,
        SlotReleasedEvent {}
