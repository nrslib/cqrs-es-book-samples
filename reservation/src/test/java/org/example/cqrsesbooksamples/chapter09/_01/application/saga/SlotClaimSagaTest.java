// 紙面照合: 9.16

package org.example.cqrsesbooksamples.chapter09._01.application.saga;

import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.AcceptReservationRequestCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.FailReservationRequestCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.RequestTransitionResult;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.ClaimSlotCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.ClaimSlotResult;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.ReleaseSlotCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.ReleaseSlotResult;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationCancelledEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationRequestAcceptedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationRequestFailedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationRequestedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.SlotClaimedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.SlotClaimFailedEvent;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.PropagatingErrorHandler;
import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SlotClaimSagaTest {

    private final CommandGateway commandGateway = mock(CommandGateway.class);
    private final SagaTestFixture<SlotClaimSaga> fixture = new SagaTestFixture<>(SlotClaimSaga.class);

    @BeforeEach
    void setUp() {
        when(commandGateway.sendAndWait(any(ClaimSlotCommand.class)))
            .thenReturn(ClaimSlotResult.CLAIMED);
        when(commandGateway.sendAndWait(any(AcceptReservationRequestCommand.class)))
            .thenReturn(RequestTransitionResult.APPLIED);
        when(commandGateway.sendAndWait(any(FailReservationRequestCommand.class)))
            .thenReturn(RequestTransitionResult.APPLIED);
        when(commandGateway.sendAndWait(any(ReleaseSlotCommand.class)))
            .thenReturn(ReleaseSlotResult.RELEASED);
        fixture.registerCommandGateway(CommandGateway.class, commandGateway);
        fixture.registerListenerInvocationErrorHandler(PropagatingErrorHandler.instance());
    }

    @Test
    void 申し込みを受けたら枠確保を指示する() {
        fixture.givenNoPriorActivity()
            .whenAggregate("r-1").publishes(new ReservationRequestedEvent("r-1", "p-1", "s-1"))
            .expectActiveSagas(1)
            .expectDispatchedCommands(new ClaimSlotCommand("s-1", "r-1"))
            .expectNoScheduledDeadlines();
    }

    @Test
    void すでに確保済みなら受理を指示する() {
        when(commandGateway.sendAndWait(any(ClaimSlotCommand.class)))
            .thenReturn(ClaimSlotResult.ALREADY_CLAIMED);

        fixture.givenNoPriorActivity()
            .whenAggregate("r-1").publishes(new ReservationRequestedEvent("r-1", "p-1", "s-1"))
            .expectActiveSagas(1)
            .expectDispatchedCommands(
                new ClaimSlotCommand("s-1", "r-1"),
                new AcceptReservationRequestCommand("r-1"));
    }

    @Test
    void すでに確保済みで受理より先にキャンセル済みなら解放して終了する() {
        when(commandGateway.sendAndWait(any(ClaimSlotCommand.class)))
            .thenReturn(ClaimSlotResult.ALREADY_CLAIMED);
        when(commandGateway.sendAndWait(any(AcceptReservationRequestCommand.class)))
            .thenReturn(RequestTransitionResult.ALREADY_CANCELLED);

        fixture.givenNoPriorActivity()
            .whenAggregate("r-1").publishes(new ReservationRequestedEvent("r-1", "p-1", "s-1"))
            .expectDispatchedCommands(
                new ClaimSlotCommand("s-1", "r-1"),
                new AcceptReservationRequestCommand("r-1"),
                new ReleaseSlotCommand("s-1", "r-1"))
            .expectActiveSagas(0);
    }

    @Test
    void 確保成功を受けたら受理を指示する() {
        fixture.givenAggregate("r-1").published(new ReservationRequestedEvent("r-1", "p-1", "s-1"))
            .whenAggregate("s-1").publishes(new SlotClaimedEvent("s-1", "r-1"))
            .expectDispatchedCommands(new AcceptReservationRequestCommand("r-1"))
            .expectActiveSagas(1)
            .expectNoScheduledDeadlines();
    }

    @Test
    void 確保の失敗を受けたら予約を失敗へ倒す() {
        when(commandGateway.sendAndWait(any(ClaimSlotCommand.class)))
            .thenReturn(ClaimSlotResult.FULL);

        fixture.givenAggregate("r-1").published(new ReservationRequestedEvent("r-1", "p-1", "s-1"))
            .whenAggregate("s-1").publishes(new SlotClaimFailedEvent("s-1", "r-1"))
            .expectDispatchedCommands(
                new FailReservationRequestCommand("r-1", "予約枠に空きがありません"))
            .expectActiveSagas(1);
    }

    @Test
    void 確保の結果が届く前のキャンセルは結果を待つ() {
        fixture.givenAggregate("r-1").published(new ReservationRequestedEvent("r-1", "p-1", "s-1"))
            .whenAggregate("r-1").publishes(new ReservationCancelledEvent("r-1", "s-1", "参加者都合"))
            .expectActiveSagas(1)
            .expectNoDispatchedCommands();
    }

    @Test
    void キャンセル済みの予約の確保が成功したら解放して終了する() {
        fixture.givenAggregate("r-1").published(new ReservationRequestedEvent("r-1", "p-1", "s-1"))
            .andThenAggregate("r-1").published(new ReservationCancelledEvent("r-1", "s-1", "参加者都合"))
            .whenAggregate("s-1").publishes(new SlotClaimedEvent("s-1", "r-1"))
            .expectDispatchedCommands(new ReleaseSlotCommand("s-1", "r-1"))
            .expectActiveSagas(0);
    }

    @Test
    void キャンセル済みの予約の確保が失敗したらそのまま終了する() {
        fixture.givenAggregate("r-1").published(new ReservationRequestedEvent("r-1", "p-1", "s-1"))
            .andThenAggregate("r-1").published(new ReservationCancelledEvent("r-1", "s-1", "参加者都合"))
            .whenAggregate("s-1").publishes(new SlotClaimFailedEvent("s-1", "r-1"))
            .expectNoDispatchedCommands()
            .expectActiveSagas(0);
    }

    @Test
    void 受理待ちのままキャンセルされたら枠を解放して終了する() {
        fixture.givenAggregate("r-1").published(new ReservationRequestedEvent("r-1", "p-1", "s-1"))
            .andThenAggregate("s-1").published(new SlotClaimedEvent("s-1", "r-1"))
            .whenAggregate("r-1").publishes(new ReservationCancelledEvent("r-1", "s-1", "参加者都合"))
            .expectDispatchedCommands(new ReleaseSlotCommand("s-1", "r-1"))
            .expectActiveSagas(0);
    }

    @Test
    void 確保失敗の後のキャンセルは解放せずに終了する() {
        fixture.givenAggregate("r-1").published(new ReservationRequestedEvent("r-1", "p-1", "s-1"))
            .andThenAggregate("s-1").published(new SlotClaimFailedEvent("s-1", "r-1"))
            .whenAggregate("r-1").publishes(new ReservationCancelledEvent("r-1", "s-1", "参加者都合"))
            .expectNoDispatchedCommands()
            .expectActiveSagas(0);
    }

    @Test
    void 受理より先にキャンセルが成立していたら枠を解放して終了する() {
        when(commandGateway.sendAndWait(any(AcceptReservationRequestCommand.class)))
            .thenReturn(RequestTransitionResult.ALREADY_CANCELLED);

        fixture.givenAggregate("r-1").published(new ReservationRequestedEvent("r-1", "p-1", "s-1"))
            .whenAggregate("s-1").publishes(new SlotClaimedEvent("s-1", "r-1"))
            .expectDispatchedCommands(
                new AcceptReservationRequestCommand("r-1"),
                new ReleaseSlotCommand("s-1", "r-1"))
            .expectActiveSagas(0);
    }

    @Test
    void 失敗より先にキャンセルが成立していたらそのまま終了する() {
        when(commandGateway.sendAndWait(any(FailReservationRequestCommand.class)))
            .thenReturn(RequestTransitionResult.ALREADY_CANCELLED);

        fixture.givenAggregate("r-1").published(new ReservationRequestedEvent("r-1", "p-1", "s-1"))
            .whenAggregate("s-1").publishes(new SlotClaimFailedEvent("s-1", "r-1"))
            .expectDispatchedCommands(
                new FailReservationRequestCommand("r-1", "予約枠に空きがありません"))
            .expectActiveSagas(0);
    }

    @Test
    void 解放コマンドの例外は伝播してサガが終了しない() {
        var exception = new IllegalStateException("枠の解放に失敗しました");
        when(commandGateway.sendAndWait(any(ReleaseSlotCommand.class)))
            .thenThrow(exception);

        assertThatThrownBy(() -> fixture
            .givenAggregate("r-1").published(new ReservationRequestedEvent("r-1", "p-1", "s-1"))
            .andThenAggregate("s-1").published(new SlotClaimedEvent("s-1", "r-1"))
            .whenAggregate("r-1").publishes(
                new ReservationCancelledEvent("r-1", "s-1", "参加者都合")))
            .hasRootCause(exception);

        fixture.whenAggregate("r-2").publishes(
                new ReservationCancelledEvent("r-2", "s-2", "参加者都合"))
            .expectActiveSagas(1);
    }

    @Test
    void 確保コマンドの例外は伝播する() {
        var exception = new IllegalStateException("枠の確保に失敗しました");
        when(commandGateway.sendAndWait(any(ClaimSlotCommand.class)))
            .thenThrow(exception);

        assertThatThrownBy(() -> fixture.givenNoPriorActivity()
            .whenAggregate("r-1").publishes(new ReservationRequestedEvent("r-1", "p-1", "s-1")))
            .hasRootCause(exception);
    }

    @Test
    void 受理のイベントでサガが終了する() {
        fixture.givenAggregate("r-1").published(new ReservationRequestedEvent("r-1", "p-1", "s-1"))
            .andThenAggregate("s-1").published(new SlotClaimedEvent("s-1", "r-1"))
            .whenAggregate("r-1").publishes(new ReservationRequestAcceptedEvent("r-1"))
            .expectNoDispatchedCommands()
            .expectActiveSagas(0);
    }

    @Test
    void 失敗のイベントでサガが終了する() {
        fixture.givenAggregate("r-1").published(new ReservationRequestedEvent("r-1", "p-1", "s-1"))
            .andThenAggregate("s-1").published(new SlotClaimFailedEvent("s-1", "r-1"))
            .whenAggregate("r-1").publishes(new ReservationRequestFailedEvent(
                "r-1", "s-1", "予約枠に空きがありません"))
            .expectNoDispatchedCommands()
            .expectActiveSagas(0);
    }
}
