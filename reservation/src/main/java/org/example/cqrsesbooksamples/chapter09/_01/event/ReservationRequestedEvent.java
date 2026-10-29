// 紙面照合: 9.4

package org.example.cqrsesbooksamples.chapter09._01.event;

public record ReservationRequestedEvent(String reservationId, String participantId, String slotId) implements ReservationEvent {}
