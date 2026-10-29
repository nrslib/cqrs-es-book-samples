package org.example.cqrsesbooksamples.chapter09._24;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.CancelOverbookedReservationCommand;
import org.example.cqrsesbooksamples.chapter09._01.event.SlotClaimedEvent;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OverbookingReconciliationHandlerTest {

    private final SlotAvailabilityRepository slotAvailabilityRepository =
        mock(SlotAvailabilityRepository.class);
    private final CommandGateway commandGateway = mock(CommandGateway.class);
    private final OverbookingReconciliationHandler handler =
        new OverbookingReconciliationHandler(slotAvailabilityRepository, commandGateway);

    @Test
    void 定員内なら予約を維持する() {
        when(slotAvailabilityRepository.countClaimed("s-1")).thenReturn(1L);
        when(slotAvailabilityRepository.getCapacity("s-1")).thenReturn(1L);

        handler.on(new SlotClaimedEvent("s-1", "r-1"));

        verifyNoInteractions(commandGateway);
    }

    @Test
    void 定員超過なら専用コマンドで予約を取り消す() {
        when(slotAvailabilityRepository.countClaimed("s-1")).thenReturn(2L);
        when(slotAvailabilityRepository.getCapacity("s-1")).thenReturn(1L);

        handler.on(new SlotClaimedEvent("s-1", "r-2"));

        verify(commandGateway).sendAndWait(
            new CancelOverbookedReservationCommand("r-2", "定員超過のため取り消されました"));
    }
}
