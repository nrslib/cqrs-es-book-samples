// 紙面照合: 9.4

package org.example.cqrsesbooksamples.chapter09._01.event;

public record ReservationRequestFailedEvent(String reservationId, String slotId, String reason) implements ReservationEvent {}
