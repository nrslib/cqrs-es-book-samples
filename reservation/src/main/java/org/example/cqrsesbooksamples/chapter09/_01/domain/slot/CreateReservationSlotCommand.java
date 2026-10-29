// 紙面照合: 9.7

package org.example.cqrsesbooksamples.chapter09._01.domain.slot;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateReservationSlotCommand(
    @TargetAggregateIdentifier String slotId,
    LocalDate slotDate,
    LocalTime startTime,
    LocalTime endTime,
    int capacity
) {}
