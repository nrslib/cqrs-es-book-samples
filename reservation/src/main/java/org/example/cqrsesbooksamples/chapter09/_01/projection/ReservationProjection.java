// 紙面照合: 9.13

package org.example.cqrsesbooksamples.chapter09._01.projection;

import org.example.cqrsesbooksamples.chapter09._01.event.*;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("reservation-projection")
@Order(1)
public class ReservationProjection {

    private final SlotAvailabilityRepository slotAvailabilityRepository;
    private final ReservationViewRepository reservationViewRepository;

    public ReservationProjection(SlotAvailabilityRepository slotAvailabilityRepository, ReservationViewRepository reservationViewRepository) {
        this.slotAvailabilityRepository = slotAvailabilityRepository;
        this.reservationViewRepository = reservationViewRepository;
    }

    @ResetHandler
    public void reset() {
        reservationViewRepository.deleteAllInBatch();
        slotAvailabilityRepository.deleteAllInBatch();
    }

    @EventHandler
    public void on(ReservationSlotCreatedEvent event) {
        slotAvailabilityRepository.save(new SlotAvailability(event.slotId(), event.slotDate(), event.capacity(), event.capacity()));
    }

    @EventHandler
    public void on(SlotClaimedEvent event) {
        slotAvailabilityRepository.decrementRemaining(event.slotId());
    }

    @EventHandler
    public void on(SlotReleasedEvent event) {
        slotAvailabilityRepository.incrementRemaining(event.slotId());
    }

    @EventHandler
    public void on(ReservationRequestedEvent event) {
        reservationViewRepository.save(new ReservationView(event.reservationId(), event.slotId(), event.participantId(), "TENTATIVE"));
    }

    @EventHandler
    public void on(ReservationRequestAcceptedEvent event) {
        reservationViewRepository.updateStatus(event.reservationId(), "SLOT_SECURED");
    }

    @EventHandler
    public void on(ReservationRequestFailedEvent event) {
        reservationViewRepository.updateStatus(event.reservationId(), "FAILED");
    }

    @EventHandler
    public void on(ReservationApprovedEvent event) {
        reservationViewRepository.updateStatus(event.reservationId(), "CONFIRMED");
    }

    @EventHandler
    public void on(ReservationRejectedEvent event) {
        reservationViewRepository.updateStatus(event.reservationId(), "REJECTED");
    }

    @EventHandler
    public void on(ReservationCancelledEvent event) {
        reservationViewRepository.updateStatus(event.reservationId(), "CANCELLED");
    }

    @EventHandler
    public void on(ReservationCompletedEvent event) {
        reservationViewRepository.updateStatus(event.reservationId(), "COMPLETED");
    }

    @EventHandler
    public void on(OverbookingResolutionRequiredEvent event) {
        reservationViewRepository.updateStatus(event.reservationId(), "COMPLETED_REQUIRES_RECONCILIATION");
    }
}
