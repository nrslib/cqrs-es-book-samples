package org.example.cqrsesbooksamples.chapter09._01.application;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.ReleaseSlotCommand;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationCancelledEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationRejectedEvent;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ReservationCancellationHandlerTest {

    private final CommandGateway commandGateway = mock(CommandGateway.class);
    private final ReservationCancellationHandler handler =
        new ReservationCancellationHandler(commandGateway);

    @Test
    void キャンセル時に確保した枠を解放する() {
        handler.on(new ReservationCancelledEvent("r-1", "s-1", "参加者都合"));

        verify(commandGateway).sendAndWait(new ReleaseSlotCommand("s-1", "r-1"));
    }

    @Test
    void 却下時に確保した枠を解放する() {
        handler.on(new ReservationRejectedEvent("r-1", "s-1", "admin-1", "主催者都合"));

        verify(commandGateway).sendAndWait(new ReleaseSlotCommand("s-1", "r-1"));
    }
}
