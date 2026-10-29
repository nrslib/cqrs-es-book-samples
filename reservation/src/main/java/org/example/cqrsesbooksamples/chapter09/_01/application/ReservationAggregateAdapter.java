// 紙面照合: 9.5

package org.example.cqrsesbooksamples.chapter09._01.application;

import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.*;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationRequestedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class ReservationAggregateAdapter {

    @AggregateIdentifier
    private String reservationId;
    private Reservation reservation;

    protected ReservationAggregateAdapter() {}

    @CommandHandler
    public ReservationAggregateAdapter(RequestReservationCommand command) {
        AggregateLifecycle.apply(
            Reservation.request(command.reservationId(), command.participantId(), command.slotId())
        );
    }

    @CommandHandler
    public RequestTransitionResult handle(AcceptReservationRequestCommand command) {
        var event = reservation.acceptRequest();
        if (event.isEmpty()) {
            return RequestTransitionResult.ALREADY_CANCELLED;
        }
        AggregateLifecycle.apply(event.get());
        return RequestTransitionResult.APPLIED;
    }

    @CommandHandler
    public RequestTransitionResult handle(FailReservationRequestCommand command) {
        var event = reservation.failRequest(command.reason());
        if (event.isEmpty()) {
            return RequestTransitionResult.ALREADY_CANCELLED;
        }
        AggregateLifecycle.apply(event.get());
        return RequestTransitionResult.APPLIED;
    }

    @CommandHandler
    public void handle(ApproveReservationCommand command) {
        AggregateLifecycle.apply(reservation.approve(command.approvedBy()));
    }

    @CommandHandler
    public void handle(RejectReservationCommand command) {
        AggregateLifecycle.apply(reservation.reject(command.rejectedBy(), command.reason()));
    }

    @CommandHandler
    public void handle(CancelReservationCommand command) {
        AggregateLifecycle.apply(reservation.cancel(command.reason()));
    }

    @CommandHandler
    public void handle(CancelOverbookedReservationCommand command) {
        reservation.cancelOverbooked(command.reason()).ifPresent(AggregateLifecycle::apply);
    }

    @CommandHandler
    public void handle(CompleteReservationCommand command) {
        AggregateLifecycle.apply(reservation.complete());
    }

    @EventSourcingHandler
    public void on(ReservationEvent event) {
        if (event instanceof ReservationRequestedEvent e) {
            this.reservationId = e.reservationId();
            this.reservation = Reservation.from(e);
        } else {
            this.reservation = reservation.applyEvent(event);
        }
    }
}
