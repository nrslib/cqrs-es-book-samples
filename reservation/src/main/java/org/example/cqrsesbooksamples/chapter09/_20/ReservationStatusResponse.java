package org.example.cqrsesbooksamples.chapter09._20;

import org.example.cqrsesbooksamples.chapter09._01.projection.ReservationView;

public record ReservationStatusResponse(
    String reservationId,
    String slotId,
    String participantId,
    String status
) {
    public static ReservationStatusResponse from(ReservationView view) {
        return new ReservationStatusResponse(
            view.getReservationId(),
            view.getSlotId(),
            view.getParticipantId(),
            view.getStatus());
    }
}
