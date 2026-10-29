// 紙面照合: 9.4

package org.example.cqrsesbooksamples.chapter09._01.event;

public sealed interface ReservationEvent permits
        ReservationRequestedEvent,
        ReservationRequestAcceptedEvent,
        ReservationRequestFailedEvent,
        ReservationApprovedEvent,
        ReservationRejectedEvent,
        ReservationCancelledEvent,
        ReservationCompletedEvent,
        OverbookingResolutionRequiredEvent {}
