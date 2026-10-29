package org.example.cqrsesbooksamples.chapter09._22;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.CreateReservationSlotCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class SubSlotCreationServiceTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 3, 5})
    void 正の定員を4等分できなければ作成コマンドを送らない(int capacity) {
        var commandGateway = mock(CommandGateway.class);
        var service = new SubSlotCreationService(commandGateway);

        assertThatThrownBy(() -> service.createSlotWithSubSlots(
            "s-1", LocalDate.of(2030, 1, 2), LocalTime.of(10, 0), LocalTime.of(11, 0), capacity))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(commandGateway);
    }

    @Test
    void 定員を4つのサブ枠へ均等に分けて作成する() {
        var commandGateway = mock(CommandGateway.class);
        var service = new SubSlotCreationService(commandGateway);
        var date = LocalDate.of(2030, 1, 2);

        service.createSlotWithSubSlots(
            "s-1", date, LocalTime.of(10, 0), LocalTime.of(11, 0), 20);

        var captor = ArgumentCaptor.forClass(CreateReservationSlotCommand.class);
        verify(commandGateway, times(4)).sendAndWait(captor.capture());
        assertThat(captor.getAllValues())
            .extracting(CreateReservationSlotCommand::slotId)
            .containsExactly("s-1-sub-0", "s-1-sub-1", "s-1-sub-2", "s-1-sub-3");
        assertThat(captor.getAllValues())
            .extracting(CreateReservationSlotCommand::capacity)
            .containsOnly(5);
    }
}
