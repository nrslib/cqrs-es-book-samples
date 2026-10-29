package org.example.cqrsesbooksamples.chapter09._24;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.axonframework.eventhandling.StreamingEventProcessor;
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry;
import org.example.cqrsesbooksamples.chapter09._01.application.ReservationAggregateAdapter;
import org.example.cqrsesbooksamples.chapter09._01.application.ReservationCancellationHandler;
import org.example.cqrsesbooksamples.chapter09._01.application.ReservationSlotAggregateAdapter;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.AcceptReservationRequestCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.ApproveReservationCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.CancelOverbookedReservationCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.CancelReservationCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.CompleteReservationCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.RequestReservationCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.ClaimSlotCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.CreateReservationSlotCommand;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationCancelledEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.OverbookingResolutionRequiredEvent;
import org.example.cqrsesbooksamples.chapter09._01.event.SlotReleasedEvent;
import org.example.cqrsesbooksamples.chapter09._01.projection.ReservationProjection;
import org.example.cqrsesbooksamples.chapter09._01.projection.ReservationViewRepository;
import org.example.cqrsesbooksamples.chapter09._01.projection.SlotAvailability;
import org.example.cqrsesbooksamples.chapter09._20.ReservationStatusQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(classes = OverbookingReconciliationIntegrationTest.TestApplication.class, properties = {
    "spring.datasource.url=jdbc:h2:mem:reservation_overbooking;DB_CLOSE_DELAY=-1",
    "axon.eventhandling.processors.reservation-projection.mode=pooled",
    "axon.eventhandling.processors.reservation-projection.initial-segment-count=1",
    "axon.eventhandling.processors.reservation-projection.batch-size=1"
})
class OverbookingReconciliationIntegrationTest {

    @Autowired CommandGateway commandGateway;
    @Autowired CommandBus commandBus;
    @Autowired EventStore eventStore;
    @Autowired EventProcessingConfiguration eventProcessing;
    @Autowired org.example.cqrsesbooksamples.chapter09._01.projection.SlotAvailabilityRepository slotAvailabilityRepository;
    @Autowired ReservationViewRepository reservationViewRepository;
    @Autowired ReservationStatusQueryService reservationStatusQueryService;

    @Test
    void 遅れた超過検知で既取消を再取消しても後続の枠解放を投影できる() {
        var slotId = "overbooked-slot";
        var reservationId = "cancelled-reservation";
        commandGateway.sendAndWait(new CreateReservationSlotCommand(
            slotId, LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0), 1));
        commandGateway.sendAndWait(new ClaimSlotCommand(slotId, "first-reservation"));
        await().untilAsserted(() -> assertThat(slotAvailabilityRepository.findById(slotId))
            .hasValueSatisfying(slot -> assertThat(slot.getRemaining()).isZero()));

        var processor = eventProcessing.eventProcessor("reservation-projection", StreamingEventProcessor.class)
            .orElseThrow();
        processor.shutDown();
        var cancellations = new CopyOnWriteArrayList<CancelOverbookedReservationCommand>();
        var registration = commandBus.registerHandlerInterceptor((unitOfWork, interceptorChain) -> {
            if (unitOfWork.getMessage().getPayload() instanceof CancelOverbookedReservationCommand command) {
                cancellations.add(command);
            }
            return interceptorChain.proceed();
        });
        try {
            commandGateway.sendAndWait(new RequestReservationCommand(reservationId, "participant", slotId));
            // 定員チェックを外した例のイベントを、既存の枠のストリームに記録する。
            var overbookedSlot = new ReservationSlot(slotId, 1, Set.of("first-reservation"));
            var claimed = overbookedSlot.claimSlot(reservationId).orElseThrow();
            eventStore.publish(new GenericDomainEventMessage<>(
                "ReservationSlotAggregateAdapter", slotId, 2, claimed));
            commandGateway.sendAndWait(new AcceptReservationRequestCommand(reservationId));
            commandGateway.sendAndWait(new CancelReservationCommand(reservationId, "参加者都合"));
            await().untilAsserted(() -> assertThat(eventStore.readEvents(slotId).asStream()
                .<Object>map(message -> message.getPayload()).filter(SlotReleasedEvent.class::isInstance).toList())
                .containsExactly(new SlotReleasedEvent(slotId, reservationId)));

            var releasePosition = eventStore.createHeadToken();
            processor.start();
            await().until(() -> !processor.processingStatus().isEmpty()
                && processor.processingStatus().values().stream().allMatch(status ->
                    status.getTrackingToken() != null && status.getTrackingToken().covers(releasePosition)));

            assertThat(cancellations).containsExactly(
                new CancelOverbookedReservationCommand(reservationId, "定員超過のため取り消されました"));
            assertThat(slotAvailabilityRepository.findById(slotId).orElseThrow().getRemaining()).isZero();
            assertThat(reservationViewRepository.findById(reservationId).orElseThrow().getStatus())
                .isEqualTo("CANCELLED");
            assertThat(eventStore.readEvents(reservationId).asStream()
                .<Object>map(message -> message.getPayload()).filter(ReservationCancelledEvent.class::isInstance).toList())
                .containsExactly(new ReservationCancelledEvent(reservationId, slotId, "参加者都合"));
        } finally {
            registration.cancel();
            processor.start();
        }
    }

    @Test
    void 完了済みの超過は手動対応として残り後続イベントとリビルドを妨げない() {
        var slotId = "completed-overbooked-slot";
        var reservationId = "completed-reservation";
        commandGateway.sendAndWait(new CreateReservationSlotCommand(
            slotId, LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0), 1));
        commandGateway.sendAndWait(new ClaimSlotCommand(slotId, "first-completed-slot-reservation"));
        await().untilAsserted(() -> assertThat(slotAvailabilityRepository.findById(slotId))
            .hasValueSatisfying(slot -> assertThat(slot.getRemaining()).isZero()));

        var processor = eventProcessing.eventProcessor("reservation-projection", StreamingEventProcessor.class)
            .orElseThrow();
        processor.shutDown();
        try {
            commandGateway.sendAndWait(new RequestReservationCommand(reservationId, "participant", slotId));
            var overbookedSlot = new ReservationSlot(slotId, 1, Set.of("first-completed-slot-reservation"));
            eventStore.publish(new GenericDomainEventMessage<>("ReservationSlotAggregateAdapter", slotId, 2,
                overbookedSlot.claimSlot(reservationId).orElseThrow()));
            commandGateway.sendAndWait(new AcceptReservationRequestCommand(reservationId));
            commandGateway.sendAndWait(new ApproveReservationCommand(reservationId, "organizer"));
            commandGateway.sendAndWait(new CompleteReservationCommand(reservationId));
            var followingSlotId = "following-slot";
            commandGateway.sendAndWait(new CreateReservationSlotCommand(
                followingSlotId, LocalDate.now().plusDays(1), LocalTime.of(12, 0), LocalTime.of(13, 0), 2));

            processor.start();
            await().untilAsserted(() -> {
                assertThat(reservationStatusQueryService.findById(reservationId))
                    .hasValueSatisfying(reservation -> assertThat(reservation.status())
                        .isEqualTo("COMPLETED_REQUIRES_RECONCILIATION"));
                assertThat(slotAvailabilityRepository.findById(followingSlotId))
                    .hasValueSatisfying(slot -> assertThat(slot.getRemaining()).isEqualTo(2));
            });
            commandGateway.sendAndWait(new CancelOverbookedReservationCommand(reservationId, "超過の再検出"));
            assertThat(eventStore.readEvents(reservationId).asStream().<Object>map(message -> message.getPayload())
                .filter(event -> event instanceof OverbookingResolutionRequiredEvent
                    || event instanceof ReservationCancelledEvent).toList())
                .containsExactly(new OverbookingResolutionRequiredEvent(reservationId, slotId));
            assertThat(eventStore.readEvents(slotId).asStream().map(message -> message.getPayload())
                .filter(SlotReleasedEvent.class::isInstance)).isEmpty();
            assertThat(slotAvailabilityRepository.findById(slotId).orElseThrow().getRemaining()).isEqualTo(-1);

            processor.shutDown();
            processor.resetTokens();
            assertThat(reservationStatusQueryService.findById(reservationId)).isEmpty();
            processor.start();
            await().untilAsserted(() -> assertThat(reservationStatusQueryService.findById(reservationId))
                .hasValueSatisfying(reservation -> assertThat(reservation.status())
                    .isEqualTo("COMPLETED_REQUIRES_RECONCILIATION")));
            assertThat(eventStore.readEvents(reservationId).asStream().map(message -> message.getPayload())
                .filter(OverbookingResolutionRequiredEvent.class::isInstance)).hasSize(1);
        } finally {
            processor.start();
        }
    }

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = {SlotAvailability.class, DomainEventEntry.class, TokenEntry.class})
    @EnableJpaRepositories(basePackageClasses = ReservationViewRepository.class)
    @Import({ReservationAggregateAdapter.class, ReservationSlotAggregateAdapter.class,
        ReservationCancellationHandler.class, ReservationProjection.class,
        OverbookingReconciliationHandler.class, JpaSlotAvailabilityRepository.class,
        ReservationStatusQueryService.class})
    static class TestApplication {
    }
}
