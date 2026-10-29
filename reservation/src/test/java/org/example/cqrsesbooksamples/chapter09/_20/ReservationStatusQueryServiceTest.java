package org.example.cqrsesbooksamples.chapter09._20;

import org.example.cqrsesbooksamples.chapter09._01.projection.ReservationView;
import org.example.cqrsesbooksamples.chapter09._01.projection.ReservationViewRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReservationStatusQueryServiceTest {

    @Test
    void 受付IDに対応するリードモデルを追跡結果へ変換する() {
        var repository = mock(ReservationViewRepository.class);
        when(repository.findById("r-1")).thenReturn(Optional.of(
            new ReservationView("r-1", "s-1", "p-1", "SLOT_SECURED")));
        var service = new ReservationStatusQueryService(repository);

        assertThat(service.findById("r-1")).contains(
            new ReservationStatusResponse("r-1", "s-1", "p-1", "SLOT_SECURED"));
    }
}
