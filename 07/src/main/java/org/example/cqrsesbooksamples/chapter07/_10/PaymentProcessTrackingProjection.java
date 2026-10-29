// 紙面照合: 7.8
package org.example.cqrsesbooksamples.chapter07._10;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.example.cqrsesbooksamples.chapter07._01.BankTransferRequestedEvent;
import org.example.cqrsesbooksamples.chapter07._01.CreditAcceptedEvent;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessCompletedEvent;
import org.example.cqrsesbooksamples.chapter07._01.PaymentProcessAcceptedEvent;
import org.example.cqrsesbooksamples.chapter07._09.PaymentProcessTracker;
import org.example.cqrsesbooksamples.chapter07._09.PaymentProcessTrackerRepository;
import org.example.cqrsesbooksamples.chapter07._09.TrackedEvent;
import org.springframework.stereotype.Component;


@Component
public class PaymentProcessTrackingProjection {
    private final PaymentProcessTrackerRepository repository;

    public PaymentProcessTrackingProjection(PaymentProcessTrackerRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public <T> void onPaymentEvent(T event, EventMessage<T> message) {
        var processId = message.getMetaData().get("processId");
        if (processId == null) {
            return; // プロセスIDがないイベントは無視
        }

        var status = deriveStatus(event);
        trackEvent(processId.toString(), status, message);
    }

    private String deriveStatus(Object event) {
        return switch (event) {
            case PaymentProcessAcceptedEvent e -> "STARTED";
            case CreditAcceptedEvent e -> "CREDIT_ACCEPTED";
            case BankTransferRequestedEvent e -> "BANK_TRANSFER_REQUESTED";
            case PaymentProcessCompletedEvent e -> "COMPLETED";
            default -> "UNKNOWN";
        };
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
