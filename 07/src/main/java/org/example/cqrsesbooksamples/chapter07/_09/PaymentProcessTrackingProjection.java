// 紙面照合: 7.7
package org.example.cqrsesbooksamples.chapter07._09;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.example.cqrsesbooksamples.chapter07._01.BankTransferRequestedEvent;
import org.example.cqrsesbooksamples.chapter07._01.CreditAcceptedEvent;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessCompletedEvent;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessAcceptedEvent;
import org.springframework.stereotype.Component;


@Component
public class PaymentProcessTrackingProjection {
    private final PaymentProcessTrackerRepository repository;

    public PaymentProcessTrackingProjection(PaymentProcessTrackerRepository repository) {
        this.repository = repository;
    }

    // 特定のイベントタイプごとにハンドラを定義
    @EventHandler
    public void on(PaymentProcessAcceptedEvent event, EventMessage<PaymentProcessAcceptedEvent> message) {
        var processId = message.getMetaData().get("processId").toString();
        trackEvent(processId, "STARTED", message);
    }

    @EventHandler
    public void on(CreditAcceptedEvent event, EventMessage<CreditAcceptedEvent> message) {
        var processId = message.getMetaData().get("processId").toString();
        trackEvent(processId, "CREDIT_ACCEPTED", message);
    }

    @EventHandler
    public void on(BankTransferRequestedEvent event, EventMessage<BankTransferRequestedEvent> message) {
        var processId = message.getMetaData().get("processId").toString();
        trackEvent(processId, "BANK_TRANSFER_REQUESTED", message);
    }

    @EventHandler
    public void on(PaymentProcessCompletedEvent event, EventMessage<PaymentProcessCompletedEvent> message) {
        var processId = message.getMetaData().get("processId").toString();
        trackEvent(processId, "COMPLETED", message);
    }

    private void trackEvent(String processId, String status, EventMessage<?> message) {
        var tracker = repository.findById(processId)
            .orElseGet(() -> {
                var newTracker = new PaymentProcessTracker();
                newTracker.setProcessId(processId);
                return newTracker;
            });

        tracker.setCurrentStatus(status);
        tracker.setLastEventId(message.getIdentifier());
        tracker.setLastUpdated(message.getTimestamp());

        tracker.getEvents().add(new TrackedEvent(
            message.getPayloadType().getSimpleName(),
            message.getIdentifier(),
            message.getTimestamp()
        ));

        repository.save(tracker);
    }
}
