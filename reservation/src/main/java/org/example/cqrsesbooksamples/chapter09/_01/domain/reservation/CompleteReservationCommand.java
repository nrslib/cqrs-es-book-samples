// 紙面照合: 9.3

package org.example.cqrsesbooksamples.chapter09._01.domain.reservation;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record CompleteReservationCommand(@TargetAggregateIdentifier String reservationId) {}
