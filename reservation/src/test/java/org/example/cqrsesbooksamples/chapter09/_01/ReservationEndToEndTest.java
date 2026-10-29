package org.example.cqrsesbooksamples.chapter09._01;

import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.ApproveReservationCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.CancelReservationCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.RequestReservationCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.ClaimSlotCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.CreateReservationSlotCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.ReleaseSlotCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.ReleaseSlotResult;
import org.example.cqrsesbooksamples.chapter09._01.event.SlotClaimedEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.SlotReleasedEvent;
import org.example.cqrsesbooksamples.chapter09._01.projection.ReservationViewRepository;
import org.example.cqrsesbooksamples.chapter09._01.projection.SlotAvailabilityRepository;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.StreamingEventProcessor;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:reservation_e2e;DB_CLOSE_DELAY=-1")
class ReservationEndToEndTest {

    @Autowired CommandGateway commandGateway;
    @Autowired CommandBus commandBus;
    @Autowired EventStore eventStore;
    @Autowired EventProcessingConfiguration eventProcessing;
    @Autowired SlotAvailabilityRepository slotAvailabilityRepository;
    @Autowired ReservationViewRepository reservationViewRepository;

    @Test
    void 申し込みから承認とキャンセルまでの一連の流れが動く() {
        var slotId = UUID.randomUUID().toString();
        commandGateway.sendAndWait(
            new CreateReservationSlotCommand(slotId, LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0), 1));

        var r1 = UUID.randomUUID().toString();
        commandGateway.sendAndWait(new RequestReservationCommand(r1, "p-1", slotId));

        await().ignoreExceptions().untilAsserted(() -> {
            assertThat(reservationViewRepository.findById(r1).orElseThrow().getStatus()).isEqualTo("SLOT_SECURED");
            assertThat(slotAvailabilityRepository.findById(slotId).orElseThrow().getRemaining()).isEqualTo(0);
        });

        var r2 = UUID.randomUUID().toString();
        commandGateway.sendAndWait(new RequestReservationCommand(r2, "p-2", slotId));

        await().ignoreExceptions().untilAsserted(() ->
            assertThat(reservationViewRepository.findById(r2).orElseThrow().getStatus()).isEqualTo("FAILED"));

        commandGateway.sendAndWait(new ApproveReservationCommand(r1, "admin-1"));
        await().ignoreExceptions().untilAsserted(() ->
            assertThat(reservationViewRepository.findById(r1).orElseThrow().getStatus()).isEqualTo("CONFIRMED"));

        commandGateway.sendAndWait(new CancelReservationCommand(r1, "体調不良"));
        await().ignoreExceptions().untilAsserted(() -> {
            assertThat(reservationViewRepository.findById(r1).orElseThrow().getStatus()).isEqualTo("CANCELLED");
            assertThat(slotAvailabilityRepository.findById(slotId).orElseThrow().getRemaining()).isEqualTo(1);
        });
    }

    @Test
    void 定員一の枠への並行申し込みは一件だけ確保される() throws InterruptedException {
        var slotId = UUID.randomUUID().toString();
        commandGateway.sendAndWait(
            new CreateReservationSlotCommand(slotId, LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0), 1));

        var r1 = UUID.randomUUID().toString();
        var r2 = UUID.randomUUID().toString();
        var requestsReady = new CountDownLatch(2);
        var startRequests = new CountDownLatch(1);
        var firstRequest = sendAfterStartSignal(
            new RequestReservationCommand(r1, "p-1", slotId), requestsReady, startRequests);
        var secondRequest = sendAfterStartSignal(
            new RequestReservationCommand(r2, "p-2", slotId), requestsReady, startRequests);

        assertThat(requestsReady.await(5, TimeUnit.SECONDS)).isTrue();
        startRequests.countDown();
        CompletableFuture.allOf(firstRequest, secondRequest).join();

        await().ignoreExceptions().untilAsserted(() -> {
            var statuses = List.of(
                reservationViewRepository.findById(r1).orElseThrow().getStatus(),
                reservationViewRepository.findById(r2).orElseThrow().getStatus());
            assertThat(statuses).containsExactlyInAnyOrder("SLOT_SECURED", "FAILED");
            assertThat(slotAvailabilityRepository.findById(slotId).orElseThrow().getRemaining()).isZero();
        });
    }

    @Test
    void 仮予約のままキャンセルすると確保された枠が解放される() throws InterruptedException {
        var slotId = UUID.randomUUID().toString();
        commandGateway.sendAndWait(
            new CreateReservationSlotCommand(slotId, LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0), 1));

        var reservationId = UUID.randomUUID().toString();
        var claimStarted = new CountDownLatch(1);
        var continueClaim = new CountDownLatch(1);
        var initialReleaseFinished = new CountDownLatch(1);
        var registration = commandBus.registerHandlerInterceptor((unitOfWork, interceptorChain) -> {
            if (unitOfWork.getMessage().getPayload() instanceof ClaimSlotCommand command
                    && command.reservationId().equals(reservationId)) {
                claimStarted.countDown();
                if (!continueClaim.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("枠確保コマンドを再開できませんでした");
                }
            }
            var result = interceptorChain.proceed();
            if (unitOfWork.getMessage().getPayload() instanceof ReleaseSlotCommand command
                    && command.reservationId().equals(reservationId)
                    && result == ReleaseSlotResult.NOT_HELD) {
                initialReleaseFinished.countDown();
            }
            return result;
        });

        try {
            commandGateway.sendAndWait(new RequestReservationCommand(reservationId, "p-1", slotId));
            assertThat(claimStarted.await(5, TimeUnit.SECONDS)).isTrue();
            commandGateway.sendAndWait(new CancelReservationCommand(reservationId, "参加者都合"));
            assertThat(initialReleaseFinished.await(5, TimeUnit.SECONDS)).isTrue();
        } finally {
            continueClaim.countDown();
            registration.cancel();
        }

        await().untilAsserted(() -> assertThat(eventStore.readEvents(slotId).asStream()
            .<Object>map(message -> message.getPayload())
            .filter(event -> event instanceof SlotClaimedEvent || event instanceof SlotReleasedEvent)
            .toList()).containsExactly(
                new SlotClaimedEvent(slotId, reservationId), new SlotReleasedEvent(slotId, reservationId)));

        // 解放イベントの投影完了も待ち、作成直後の残数1で成功することを防ぐ
        var releasePosition = eventStore.createHeadToken();
        var processor = eventProcessing.eventProcessor("reservation-projection", StreamingEventProcessor.class)
            .orElseThrow();
        await().until(() -> !processor.processingStatus().isEmpty()
            && processor.processingStatus().values().stream()
                .allMatch(status -> status.getTrackingToken() != null
                    && status.getTrackingToken().covers(releasePosition)));

        await().ignoreExceptions().untilAsserted(() -> {
            assertThat(reservationViewRepository.findById(reservationId).orElseThrow().getStatus())
                .isEqualTo("CANCELLED");
            assertThat(slotAvailabilityRepository.findById(slotId).orElseThrow().getRemaining()).isEqualTo(1);
        });
    }

    private CompletableFuture<Void> sendAfterStartSignal(
            RequestReservationCommand command,
            CountDownLatch requestsReady,
            CountDownLatch startRequests) {
        return CompletableFuture.runAsync(() -> {
            requestsReady.countDown();
            awaitLatch(startRequests);
            commandGateway.sendAndWait(command);
        });
    }

    private static void awaitLatch(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("並行申し込みを開始できませんでした");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("並行申し込みの待機が中断されました", exception);
        }
    }
}
