package org.example.cqrsesbooksamples.chapter09._01.event;

public record OverbookingResolutionRequiredEvent(String reservationId, String slotId) implements ReservationEvent {}
