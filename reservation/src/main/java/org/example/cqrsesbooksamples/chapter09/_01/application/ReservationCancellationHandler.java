// 削除済み（旧 9.14）
package org.example.cqrsesbooksamples.chapter09._01.application;

import org.example.cqrsesbooksamples.chapter09._01.domain.slot.ReleaseSlotCommand;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationCancelledEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationRejectedEvent;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("reservation-slot-release")
public class ReservationCancellationHandler {

    private final CommandGateway commandGateway;

    public ReservationCancellationHandler(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @EventHandler
    public void on(ReservationCancelledEvent event) {
        commandGateway.sendAndWait(
            new ReleaseSlotCommand(event.slotId(), event.reservationId()));
    }

    // 却下も確保済みの枠を解放する
    @EventHandler
    public void on(ReservationRejectedEvent event) {
        commandGateway.sendAndWait(
            new ReleaseSlotCommand(event.slotId(), event.reservationId()));
    }
}
