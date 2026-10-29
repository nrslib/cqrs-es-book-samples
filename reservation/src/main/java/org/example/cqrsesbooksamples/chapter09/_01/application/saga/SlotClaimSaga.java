// 紙面照合: 9.9, 9.10, 9.11, 9.12

package org.example.cqrsesbooksamples.chapter09._01.application.saga;

import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.AcceptReservationRequestCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.FailReservationRequestCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.RequestTransitionResult;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.ClaimSlotCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.ClaimSlotResult;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.ReleaseSlotCommand;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationCancelledEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationRequestAcceptedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationRequestFailedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationRequestedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.SlotClaimedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.SlotClaimFailedEvent;
import jakarta.inject.Inject;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;

@Saga
public class SlotClaimSaga {

    private enum State { CLAIMING, CLAIMING_CANCELLED, ACCEPTING, FAILING }

    @Inject
    private transient CommandGateway commandGateway;

    private String reservationId;
    private String slotId;
    private State state = State.CLAIMING;

    @StartSaga
    @SagaEventHandler(associationProperty = "reservationId")
    public void on(ReservationRequestedEvent event) {
        this.reservationId = event.reservationId();
        this.slotId = event.slotId();
        SagaLifecycle.associateWith("slotId", event.slotId());

        ClaimSlotResult result = commandGateway.sendAndWait(
            new ClaimSlotCommand(slotId, reservationId));
        switch (result) {
            case CLAIMED, FULL -> { }
            case ALREADY_CLAIMED -> acceptReservation();
        }
    }


    @SagaEventHandler(associationProperty = "slotId")
    public void on(SlotClaimedEvent event) {
        if (!event.reservationId().equals(this.reservationId)) return;
        switch (state) {
            case CLAIMING -> acceptReservation();
            case CLAIMING_CANCELLED, FAILING -> releaseSlotAndEnd();
            case ACCEPTING -> { /* 重複配信は無視 */ }
        }
    }

    @SagaEventHandler(associationProperty = "slotId")
    public void on(SlotClaimFailedEvent event) {
        if (!event.reservationId().equals(this.reservationId)) return;
        switch (state) {
            case CLAIMING -> failReservation("予約枠に空きがありません");
            case CLAIMING_CANCELLED -> SagaLifecycle.end();
            case ACCEPTING, FAILING -> { /* 重複配信は無視 */ }
        }
    }


    @EndSaga
    @SagaEventHandler(associationProperty = "reservationId")
    public void on(ReservationRequestAcceptedEvent event) {
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "reservationId")
    public void on(ReservationRequestFailedEvent event) {
    }

    @SagaEventHandler(associationProperty = "reservationId")
    public void on(ReservationCancelledEvent event) {
        switch (state) {
            case CLAIMING -> state = State.CLAIMING_CANCELLED;
            case CLAIMING_CANCELLED -> { /* 重複配信は無視 */ }
            case ACCEPTING -> releaseSlotAndEnd();
            case FAILING -> SagaLifecycle.end();
        }
    }


    private void acceptReservation() {
        state = State.ACCEPTING;
        RequestTransitionResult result = commandGateway.sendAndWait(
            new AcceptReservationRequestCommand(reservationId));
        if (result == RequestTransitionResult.ALREADY_CANCELLED) {
            // 受理より先にキャンセルが成立していた。確保した枠を返してサガを終了する
            releaseSlotAndEnd();
        }
    }

    private void failReservation(String reason) {
        state = State.FAILING;
        RequestTransitionResult result = commandGateway.sendAndWait(
            new FailReservationRequestCommand(reservationId, reason));
        if (result == RequestTransitionResult.ALREADY_CANCELLED) {
            // 枠は確保できていないので、解放するものはない
            SagaLifecycle.end();
        }
    }

    // 解放が失敗したらここで例外が飛び、サガの更新とトークンの更新がロールバックされる。
    // 同じイベントが再処理されるので、解放されないままサガが終了することはない
    private void releaseSlotAndEnd() {
        commandGateway.sendAndWait(new ReleaseSlotCommand(slotId, reservationId));
        SagaLifecycle.end();
    }
}
