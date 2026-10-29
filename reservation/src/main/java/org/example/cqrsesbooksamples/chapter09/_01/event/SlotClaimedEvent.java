// 削除済み（旧 9.7）
package org.example.cqrsesbooksamples.chapter09._01.event;

public record SlotClaimedEvent(String slotId, String reservationId) implements ReservationSlotEvent {}
