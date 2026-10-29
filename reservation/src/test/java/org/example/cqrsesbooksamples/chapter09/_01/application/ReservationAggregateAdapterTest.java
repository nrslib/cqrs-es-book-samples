// 紙面照合: 9.15

package org.example.cqrsesbooksamples.chapter09._01.application;

import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.AcceptReservationRequestCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.ApproveReservationCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.CancelOverbookedReservationCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.CancelReservationCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.FailReservationRequestCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.RejectReservationCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.CompleteReservationCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.RequestTransitionResult;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationApprovedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationCancelledEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationRequestAcceptedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationRequestedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationRejectedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationCompletedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.OverbookingResolutionRequiredEvent;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.Test;

class ReservationAggregateAdapterTest {

    private final AggregateTestFixture<ReservationAggregateAdapter> fixture =
        new AggregateTestFixture<>(ReservationAggregateAdapter.class);

    @Test
    void 仮予約は枠確保できる() {
        fixture.given(new ReservationRequestedEvent("r-1", "p-1", "s-1"))
            .when(new AcceptReservationRequestCommand("r-1"))
            .expectEvents(new ReservationRequestAcceptedEvent("r-1"));
    }

    @Test
    void 枠確保後に承認すると確定する() {
        fixture.given(
                new ReservationRequestedEvent("r-1", "p-1", "s-1"),
                new ReservationRequestAcceptedEvent("r-1"))
            .when(new ApproveReservationCommand("r-1", "admin-1"))
            .expectEvents(new ReservationApprovedEvent("r-1", "admin-1"));
    }

    @Test
    void 承認済みは再度承認できない() {
        fixture.given(
                new ReservationRequestedEvent("r-1", "p-1", "s-1"),
                new ReservationRequestAcceptedEvent("r-1"),
                new ReservationApprovedEvent("r-1", "admin-1"))
            .when(new ApproveReservationCommand("r-1", "admin-1"))
            .expectException(IllegalStateException.class);
    }

    @Test
    void 枠確保済みの超過予約を取り消せる() {
        fixture.given(
                new ReservationRequestedEvent("r-1", "p-1", "s-1"),
                new ReservationRequestAcceptedEvent("r-1"))
            .when(new CancelOverbookedReservationCommand(
                "r-1", "定員超過のため取り消されました"))
            .expectEvents(new ReservationCancelledEvent(
                "r-1", "s-1", "定員超過のため取り消されました"));
    }

    @Test
    void キャンセル済みの予約への超過取消はイベントを出さず成功する() {
        fixture.given(
                new ReservationRequestedEvent("r-1", "p-1", "s-1"),
                new ReservationRequestAcceptedEvent("r-1"),
                new ReservationCancelledEvent("r-1", "s-1", "参加者都合"))
            .when(new CancelOverbookedReservationCommand("r-1", "定員超過"))
            .expectSuccessfulHandlerExecution()
            .expectNoEvents();
    }

    @Test
    void キャンセル済みの予約への通常取消は引き続き拒否する() {
        fixture.given(
                new ReservationRequestedEvent("r-1", "p-1", "s-1"),
                new ReservationCancelledEvent("r-1", "s-1", "参加者都合"))
            .when(new CancelReservationCommand("r-1", "再取消"))
            .expectException(IllegalStateException.class)
            .expectNoEvents();
    }

    @Test
    void 完了済みの超過予約は取り消さず手動対応要求を記録する() {
        fixture.given(
                new ReservationRequestedEvent("r-1", "p-1", "s-1"),
                new ReservationRequestAcceptedEvent("r-1"),
                new ReservationApprovedEvent("r-1", "admin-1"),
                new ReservationCompletedEvent("r-1"))
            .when(new CancelOverbookedReservationCommand("r-1", "定員超過"))
            .expectSuccessfulHandlerExecution()
            .expectEvents(new OverbookingResolutionRequiredEvent("r-1", "s-1"));
    }

    @Test
    void 手動対応要求を履歴から復元した予約には要求を重複発行しない() {
        fixture.given(
                new ReservationRequestedEvent("r-1", "p-1", "s-1"),
                new ReservationRequestAcceptedEvent("r-1"),
                new ReservationApprovedEvent("r-1", "admin-1"),
                new ReservationCompletedEvent("r-1"),
                new OverbookingResolutionRequiredEvent("r-1", "s-1"))
            .when(new CancelOverbookedReservationCommand("r-1", "定員超過"))
            .expectSuccessfulHandlerExecution()
            .expectNoEvents();
    }

    @Test
    void 手動対応が必要な完了済み予約への通常取消は引き続き拒否する() {
        fixture.given(
                new ReservationRequestedEvent("r-1", "p-1", "s-1"),
                new ReservationRequestAcceptedEvent("r-1"),
                new ReservationApprovedEvent("r-1", "admin-1"),
                new ReservationCompletedEvent("r-1"),
                new OverbookingResolutionRequiredEvent("r-1", "s-1"))
            .when(new CancelReservationCommand("r-1", "再取消"))
            .expectException(IllegalStateException.class)
            .expectNoEvents();
    }

    @Test
    void キャンセル済みの予約への受理指示はイベントを出さない() {
        fixture.given(
                new ReservationRequestedEvent("r-1", "p-1", "s-1"),
                new ReservationCancelledEvent("r-1", "s-1", "参加者都合"))
            .when(new AcceptReservationRequestCommand("r-1"))
            .expectSuccessfulHandlerExecution()
            .expectNoEvents()
            .expectResultMessagePayload(RequestTransitionResult.ALREADY_CANCELLED);
    }

    @Test
    void キャンセル済みの予約への失敗指示はイベントを出さない() {
        fixture.given(
                new ReservationRequestedEvent("r-1", "p-1", "s-1"),
                new ReservationCancelledEvent("r-1", "s-1", "参加者都合"))
            .when(new FailReservationRequestCommand("r-1", "予約枠に空きがありません"))
            .expectSuccessfulHandlerExecution()
            .expectNoEvents()
            .expectResultMessagePayload(RequestTransitionResult.ALREADY_CANCELLED);
    }

    @Test
    void 確定済みの予約への受理指示は不正な状態遷移になる() {
        fixture.given(
                new ReservationRequestedEvent("r-1", "p-1", "s-1"),
                new ReservationRequestAcceptedEvent("r-1"),
                new ReservationApprovedEvent("r-1", "admin-1"))
            .when(new AcceptReservationRequestCommand("r-1"))
            .expectException(IllegalStateException.class);
    }

    @Test
    void 確定済みの予約への失敗指示は不正な状態遷移になる() {
        fixture.given(
                new ReservationRequestedEvent("r-1", "p-1", "s-1"),
                new ReservationRequestAcceptedEvent("r-1"),
                new ReservationApprovedEvent("r-1", "admin-1"))
            .when(new FailReservationRequestCommand("r-1", "予約枠に空きがありません"))
            .expectException(IllegalStateException.class);
    }

    @Test
    void 枠確保済みの予約を却下できる() {
        fixture.given(
                new ReservationRequestedEvent("r-1", "p-1", "s-1"),
                new ReservationRequestAcceptedEvent("r-1"))
            .when(new RejectReservationCommand("r-1", "admin-1", "定員調整のため"))
            .expectEvents(new ReservationRejectedEvent("r-1", "s-1", "admin-1", "定員調整のため"));
    }

    @Test
    void 確定済みの予約を完了にできる() {
        fixture.given(
                new ReservationRequestedEvent("r-1", "p-1", "s-1"),
                new ReservationRequestAcceptedEvent("r-1"),
                new ReservationApprovedEvent("r-1", "admin-1"))
            .when(new CompleteReservationCommand("r-1"))
            .expectEvents(new ReservationCompletedEvent("r-1"));
    }

    @Test
    void 確定前の予約は完了にできない() {
        fixture.given(
                new ReservationRequestedEvent("r-1", "p-1", "s-1"),
                new ReservationRequestAcceptedEvent("r-1"))
            .when(new CompleteReservationCommand("r-1"))
            .expectException(IllegalStateException.class);
    }
}
