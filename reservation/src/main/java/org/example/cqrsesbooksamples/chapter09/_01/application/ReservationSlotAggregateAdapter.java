// 紙面照合: 9.8

package org.example.cqrsesbooksamples.chapter09._01.application;

import org.example.cqrsesbooksamples.chapter09._01.domain.slot.*;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationSlotCreatedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationSlotEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.SlotClaimedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class ReservationSlotAggregateAdapter {

    @AggregateIdentifier
    private String slotId;
    private ReservationSlot slot;

    protected ReservationSlotAggregateAdapter() {}

    @CommandHandler
    public ReservationSlotAggregateAdapter(CreateReservationSlotCommand command) {
        AggregateLifecycle.apply(
            ReservationSlot.create(
                command.slotId(),
                command.slotDate(),
                new TimeRange(command.startTime(), command.endTime()),
                command.capacity()
            )
        );
    }

    @CommandHandler
    public ClaimSlotResult handle(ClaimSlotCommand command) {
        var event = slot.claimSlot(command.reservationId());
        if (event.isEmpty()) {
            return ClaimSlotResult.ALREADY_CLAIMED;
        }
        AggregateLifecycle.apply(event.get());
        return event.get() instanceof SlotClaimedEvent
            ? ClaimSlotResult.CLAIMED
            : ClaimSlotResult.FULL;
    }

    @CommandHandler
    public ReleaseSlotResult handle(ReleaseSlotCommand command) {
        var event = slot.releaseSlot(command.reservationId());
        if (event.isEmpty()) {
            return ReleaseSlotResult.NOT_HELD;
        }
        AggregateLifecycle.apply(event.get());
        return ReleaseSlotResult.RELEASED;
    }

    @EventSourcingHandler
    public void on(ReservationSlotEvent event) {
        if (event instanceof ReservationSlotCreatedEvent e) {
            this.slotId = e.slotId();
            this.slot = ReservationSlot.from(e);
        } else {
            this.slot = slot.applyEvent(event);
        }
    }
}
