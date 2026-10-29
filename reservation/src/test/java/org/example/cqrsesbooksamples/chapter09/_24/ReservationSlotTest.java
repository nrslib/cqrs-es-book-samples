package org.example.cqrsesbooksamples.chapter09._24;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationSlotTest {

    @Test
    void 定員を超えても異なる予約なら確保できる() {
        var slot = new ReservationSlot("s-1", 1);
        slot = slot.applyEvent(slot.claimSlot("r-1").orElseThrow());

        var secondClaim = slot.claimSlot("r-2");

        assertThat(secondClaim).isPresent();
        assertThat(secondClaim.get().reservationId()).isEqualTo("r-2");
    }

    @Test
    void 同じ予約による再確保は何も起こさない() {
        var slot = new ReservationSlot("s-1", 1);
        slot = slot.applyEvent(slot.claimSlot("r-1").orElseThrow());

        assertThat(slot.claimSlot("r-1")).isEmpty();
    }
}
