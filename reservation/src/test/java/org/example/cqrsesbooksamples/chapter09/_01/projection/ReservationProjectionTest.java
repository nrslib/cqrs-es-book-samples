// 削除済み（旧 9.19）
package org.example.cqrsesbooksamples.chapter09._01.projection;

import org.example.cqrsesbooksamples.chapter09._01.event.ReservationRequestedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.SlotClaimedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.SlotReleasedEvent;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ReservationProjectionTest {

    private final SlotAvailabilityRepository slotAvailabilityRepository = mock(SlotAvailabilityRepository.class);
    private final ReservationViewRepository reservationViewRepository = mock(ReservationViewRepository.class);
    private final ReservationProjection projection = new ReservationProjection(slotAvailabilityRepository, reservationViewRepository);

    @Test
    void 枠確保で空きが減る() {
        projection.on(new SlotClaimedEvent("s-1", "r-1"));
        verify(slotAvailabilityRepository).decrementRemaining("s-1");
    }

    @Test
    void 枠解放で空きが増える() {
        projection.on(new SlotReleasedEvent("s-1", "r-1"));
        verify(slotAvailabilityRepository).incrementRemaining("s-1");
    }

    @Test
    void 申し込みで予約一覧へ仮予約が載る() {
        projection.on(new ReservationRequestedEvent("r-1", "p-1", "s-1"));
        verify(reservationViewRepository).save(argThat(v -> "TENTATIVE".equals(v.getStatus())));
    }

    @Test
    void リプレイ前に投影を消去する() {
        projection.reset();

        verify(reservationViewRepository).deleteAllInBatch();
        verify(slotAvailabilityRepository).deleteAllInBatch();
    }
}
