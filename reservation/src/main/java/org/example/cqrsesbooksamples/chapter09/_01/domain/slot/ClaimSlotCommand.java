// 紙面照合: 9.7

package org.example.cqrsesbooksamples.chapter09._01.domain.slot;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record ClaimSlotCommand(@TargetAggregateIdentifier String slotId, String reservationId) {}
