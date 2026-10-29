package org.example.cqrsesbooksamples.chapter09._01.domain.reservation;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record CancelOverbookedReservationCommand(
    @TargetAggregateIdentifier String reservationId,
    String reason
) {}
