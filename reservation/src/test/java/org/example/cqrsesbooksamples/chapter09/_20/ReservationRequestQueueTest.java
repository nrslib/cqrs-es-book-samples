package org.example.cqrsesbooksamples.chapter09._20;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.RequestReservationCommand;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class ReservationRequestQueueTest {

    @Test
    void バーストで積んでも一回のレート周期では一件だけ送信する() {
        var commandGateway = mock(CommandGateway.class);
        var queue = new ReservationRequestQueue(commandGateway);
        var first = new RequestReservationCommand("r-1", "p-1", "s-1");
        var second = new RequestReservationCommand("r-2", "p-2", "s-1");
        var third = new RequestReservationCommand("r-3", "p-3", "s-1");
        queue.enqueue(first);
        queue.enqueue(second);
        queue.enqueue(third);

        queue.dispatchNext();

        verify(commandGateway).sendAndWait(first);
        verifyNoMoreInteractions(commandGateway);

        queue.dispatchNext();
        queue.dispatchNext();

        verify(commandGateway).sendAndWait(second);
        verify(commandGateway).sendAndWait(third);
        verify(commandGateway, times(3)).sendAndWait(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 送信が失敗した要求はキューに残り次の周期で再送される() {
        var commandGateway = mock(CommandGateway.class);
        var queue = new ReservationRequestQueue(commandGateway);
        var command = new RequestReservationCommand("r-1", "p-1", "s-1");
        queue.enqueue(command);

        doThrow(new IllegalStateException("一時的な失敗"))
            .when(commandGateway).sendAndWait(command);
        assertThatThrownBy(queue::dispatchNext)
            .isInstanceOf(IllegalStateException.class);

        org.mockito.Mockito.reset(commandGateway);
        queue.dispatchNext();

        verify(commandGateway).sendAndWait(command);
    }
}
