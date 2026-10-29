// 紙面照合: 9.21
package org.example.cqrsesbooksamples.chapter09._24;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.DisallowReplay;
import org.axonframework.eventhandling.EventHandler;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.CancelOverbookedReservationCommand;
import org.example.cqrsesbooksamples.chapter09._01.event.SlotClaimedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// リードモデルの更新（@Order(1)）より後に走らせる
@Component
@ProcessingGroup("reservation-projection")
@Order(2)
public class OverbookingReconciliationHandler {

    private final SlotAvailabilityRepository slotAvailabilityRepository;
    private final CommandGateway commandGateway;

    public OverbookingReconciliationHandler(
            SlotAvailabilityRepository slotAvailabilityRepository,
            CommandGateway commandGateway) {
        this.slotAvailabilityRepository = slotAvailabilityRepository;
        this.commandGateway = commandGateway;
    }


    // リプレイ中は超過取消を送らない。
    @DisallowReplay
    @EventHandler
    public void on(SlotClaimedEvent event) {
        var claimed = slotAvailabilityRepository.countClaimed(event.slotId());
        var capacity = slotAvailabilityRepository.getCapacity(event.slotId());
        if (claimed > capacity) {
            commandGateway.sendAndWait(
                new CancelOverbookedReservationCommand(
                    event.reservationId(), "定員超過のため取り消されました"));
        }
    }
}
