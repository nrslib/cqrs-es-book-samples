// 紙面照合: 9.2

package org.example.cqrsesbooksamples.chapter09._01.domain.reservation;

import org.example.cqrsesbooksamples.chapter09._01.event.*;

import java.util.Optional;

public record Reservation(
    String reservationId,
    String slotId,
    String participantId,
    ReservationStatus status,
    boolean overbookingResolutionRequested
) {
    public static ReservationRequestedEvent request(String reservationId, String participantId, String slotId) {
        if (participantId == null || participantId.isBlank()) {
            throw new IllegalArgumentException("参加者IDは必須です");
        }
        if (slotId == null || slotId.isBlank()) {
            throw new IllegalArgumentException("予約枠IDは必須です");
        }
        return new ReservationRequestedEvent(reservationId, participantId, slotId);
    }

    public static Reservation from(ReservationRequestedEvent event) {
        return new Reservation(event.reservationId(), event.slotId(), event.participantId(),
            ReservationStatus.TENTATIVE, false);
    }

    public Optional<ReservationRequestAcceptedEvent> acceptRequest() {
        if (status == ReservationStatus.CANCELLED) {
            return Optional.empty();
        }
        if (status != ReservationStatus.TENTATIVE) {
            throw new IllegalStateException("枠確保できるのは仮予約のときだけです");
        }
        return Optional.of(new ReservationRequestAcceptedEvent(reservationId));
    }

    public ReservationApprovedEvent approve(String approvedBy) {
        if (status != ReservationStatus.SLOT_SECURED) {
            throw new IllegalStateException("承認できるのは枠確保済みのときだけです");
        }
        return new ReservationApprovedEvent(reservationId, approvedBy);
    }

    public Optional<ReservationRequestFailedEvent> failRequest(String reason) {
        if (status == ReservationStatus.CANCELLED) {
            return Optional.empty();
        }
        if (status != ReservationStatus.TENTATIVE) {
            throw new IllegalStateException("失敗にできるのは仮予約のときだけです");
        }
        return Optional.of(new ReservationRequestFailedEvent(reservationId, slotId, reason));
    }

    public ReservationRejectedEvent reject(String rejectedBy, String reason) {
        if (status != ReservationStatus.SLOT_SECURED) {
            throw new IllegalStateException("却下できるのは枠確保済みのときだけです");
        }
        return new ReservationRejectedEvent(reservationId, slotId, rejectedBy, reason);
    }

    public ReservationCancelledEvent cancel(String reason) {
        if (status != ReservationStatus.TENTATIVE
                && status != ReservationStatus.SLOT_SECURED
                && status != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("この予約はキャンセルできません");
        }
        return new ReservationCancelledEvent(reservationId, slotId, reason);
    }

    public ReservationCompletedEvent complete() {
        if (status != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("完了にできるのは確定済みのときだけです");
        }
        return new ReservationCompletedEvent(reservationId);
    }

    public Optional<ReservationEvent> cancelOverbooked(String reason) {
        return switch (status) {
            case CANCELLED -> Optional.empty();
            case COMPLETED -> overbookingResolutionRequested
                ? Optional.empty()
                : Optional.of(new OverbookingResolutionRequiredEvent(reservationId, slotId));
            case TENTATIVE, SLOT_SECURED, CONFIRMED -> Optional.of(cancel(reason));
        };
    }

    public Reservation applyEvent(ReservationEvent event) {
        return switch (event) {
            case ReservationRequestedEvent e -> from(e);
            case ReservationRequestAcceptedEvent _ -> withStatus(ReservationStatus.SLOT_SECURED);
            case ReservationRequestFailedEvent _ -> withStatus(ReservationStatus.CANCELLED);
            case ReservationApprovedEvent _ -> withStatus(ReservationStatus.CONFIRMED);
            case ReservationRejectedEvent _ -> withStatus(ReservationStatus.CANCELLED);
            case ReservationCancelledEvent _ -> withStatus(ReservationStatus.CANCELLED);
            case ReservationCompletedEvent _ -> withStatus(ReservationStatus.COMPLETED);
            case OverbookingResolutionRequiredEvent _ ->
                new Reservation(reservationId, slotId, participantId, status, true);
        };
    }

    private Reservation withStatus(ReservationStatus newStatus) {
        return new Reservation(reservationId, slotId, participantId, newStatus, overbookingResolutionRequested);
    }
}
