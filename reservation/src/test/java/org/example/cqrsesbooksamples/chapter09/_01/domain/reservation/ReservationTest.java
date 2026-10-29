package org.example.cqrsesbooksamples.chapter09._01.domain.reservation;

import org.example.cqrsesbooksamples.chapter09._01.event.OverbookingResolutionRequiredEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationApprovedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationCompletedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationRequestAcceptedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationRequestedEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationTest {

    @Test
    void 手動対応要求を復元しても完了状態を維持し要求を重複発行しない() {
        var reservation = Reservation.from(new ReservationRequestedEvent("r-1", "p-1", "s-1"))
            .applyEvent(new ReservationRequestAcceptedEvent("r-1"))
            .applyEvent(new ReservationApprovedEvent("r-1", "admin-1"))
            .applyEvent(new ReservationCompletedEvent("r-1"));

        var event = reservation.cancelOverbooked("定員超過").orElseThrow();

        assertThat(event).isEqualTo(new OverbookingResolutionRequiredEvent("r-1", "s-1"));
        var restored = reservation.applyEvent(event);
        assertThat(restored.status()).isEqualTo(ReservationStatus.COMPLETED);
        assertThat(restored.overbookingResolutionRequested()).isTrue();
        assertThat(restored.cancelOverbooked("定員超過")).isEmpty();
    }
}
