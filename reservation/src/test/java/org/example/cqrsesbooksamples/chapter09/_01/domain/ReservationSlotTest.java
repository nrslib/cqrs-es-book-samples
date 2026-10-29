package org.example.cqrsesbooksamples.chapter09._01.domain;

import org.example.cqrsesbooksamples.chapter09._01.domain.slot.ReservationSlot;
import org.example.cqrsesbooksamples.chapter09._01.event.SlotReleasedEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationSlotTest {

    @Test
    void 確保していない予約の解放は何も起こさない() {
        var slot = new ReservationSlot("s-1", 10);

        assertThat(slot.releaseSlot("unknown")).isEmpty();
        assertThat(slot.remaining()).isEqualTo(10);
    }

    @Test
    void 確保済みの予約は解放イベントが返り適用で空きが戻る() {
        var slot = new ReservationSlot("s-1", 10);
        slot = slot.applyEvent(slot.claimSlot("r-1").orElseThrow());
        assertThat(slot.remaining()).isEqualTo(9);

        var released = slot.releaseSlot("r-1");
        assertThat(released).contains(new SlotReleasedEvent("s-1", "r-1"));
        slot = slot.applyEvent(released.orElseThrow());
        assertThat(slot.remaining()).isEqualTo(10);
    }
}
