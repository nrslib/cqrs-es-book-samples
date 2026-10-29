package org.example.cqrsesbooksamples.chapter09._24;

import org.example.cqrsesbooksamples.chapter09._01.projection.SlotAvailability;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JpaSlotAvailabilityRepositoryTest {

    @Test
    void 定員と残席から確保数と定員を返す() {
        var readModelRepository = mock(
            org.example.cqrsesbooksamples.chapter09._01.projection.SlotAvailabilityRepository.class);
        when(readModelRepository.findById("s-1")).thenReturn(Optional.of(
            new SlotAvailability("s-1", LocalDate.now().plusDays(1), 5, 2)));
        var repository = new JpaSlotAvailabilityRepository(readModelRepository);

        assertThat(repository.countClaimed("s-1")).isEqualTo(3);
        assertThat(repository.getCapacity("s-1")).isEqualTo(5);
    }
}
