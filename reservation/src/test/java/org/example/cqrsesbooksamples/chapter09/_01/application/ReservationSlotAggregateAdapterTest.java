// 検証補助: 9.8

package org.example.cqrsesbooksamples.chapter09._01.application;

import org.example.cqrsesbooksamples.chapter09._01.domain.slot.ClaimSlotCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.ClaimSlotResult;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.ReleaseSlotCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.ReleaseSlotResult;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationSlotCreatedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.SlotClaimedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.SlotClaimFailedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.SlotReleasedEvent;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

class ReservationSlotAggregateAdapterTest {

    private final AggregateTestFixture<ReservationSlotAggregateAdapter> fixture =
        new AggregateTestFixture<>(ReservationSlotAggregateAdapter.class);

    private final ReservationSlotCreatedEvent created = new ReservationSlotCreatedEvent(
        "s-1", LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0), 1);

    @Test
    void 空きがあれば確保できる() {
        fixture.given(created)
            .when(new ClaimSlotCommand("s-1", "r-1"))
            .expectEvents(new SlotClaimedEvent("s-1", "r-1"))
            .expectResultMessagePayload(ClaimSlotResult.CLAIMED);
    }

    @Test
    void 満席なら確保が失敗する() {
        fixture.given(created, new SlotClaimedEvent("s-1", "r-1"))
            .when(new ClaimSlotCommand("s-1", "r-2"))
            .expectEvents(new SlotClaimFailedEvent("s-1", "r-2"))
            .expectResultMessagePayload(ClaimSlotResult.FULL);
    }

    @Test
    void 同じ予約の再確保はイベントを出さない() {
        fixture.given(created, new SlotClaimedEvent("s-1", "r-1"))
            .when(new ClaimSlotCommand("s-1", "r-1"))
            .expectSuccessfulHandlerExecution()
            .expectNoEvents()
            .expectResultMessagePayload(ClaimSlotResult.ALREADY_CLAIMED);
    }

    @Test
    void 確保済みの予約を解放すると枠解放イベントが発生する() {
        fixture.given(created, new SlotClaimedEvent("s-1", "r-1"))
            .when(new ReleaseSlotCommand("s-1", "r-1"))
            .expectEvents(new SlotReleasedEvent("s-1", "r-1"))
            .expectResultMessagePayload(ReleaseSlotResult.RELEASED);
    }

    @Test
    void 確保していない予約の解放は何も起こさない() {
        fixture.given(created)
            .when(new ReleaseSlotCommand("s-1", "r-9"))
            .expectSuccessfulHandlerExecution()
            .expectNoEvents()
            .expectResultMessagePayload(ReleaseSlotResult.NOT_HELD);
    }

    @Test
    void 確保していない予約を解放した後でも同じ予約を確保できる() {
        fixture.given(created)
            .andGivenCommands(new ReleaseSlotCommand("s-1", "r-1"))
            .when(new ClaimSlotCommand("s-1", "r-1"))
            .expectEvents(new SlotClaimedEvent("s-1", "r-1"))
            .expectResultMessagePayload(ClaimSlotResult.CLAIMED);
    }

    @Test
    void 解放イベントの後は別の予約が確保できる() {
        fixture.given(
                created,
                new SlotClaimedEvent("s-1", "r-1"),
                new SlotReleasedEvent("s-1", "r-1"))
            .when(new ClaimSlotCommand("s-1", "r-2"))
            .expectEvents(new SlotClaimedEvent("s-1", "r-2"))
            .expectResultMessagePayload(ClaimSlotResult.CLAIMED);
    }
}
