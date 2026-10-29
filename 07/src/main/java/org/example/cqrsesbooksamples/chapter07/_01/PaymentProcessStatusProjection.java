// 紙面照合: 7.2
package org.example.cqrsesbooksamples.chapter07._01;

import java.time.LocalDateTime;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;


@Component
public class PaymentProcessStatusProjection {
    private final PaymentProcessStatusRepository repository;

    public PaymentProcessStatusProjection(PaymentProcessStatusRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(PaymentProcessAcceptedEvent event) {
        var status = new PaymentProcessStatus();
        status.setProcessId(event.paymentProcessId().value());
        status.setCurrentState(PaymentProcessState.STARTED);
        status.setLastUpdated(LocalDateTime.now());
        repository.save(status);
    }

    @EventHandler
    public void on(CreditAcceptedEvent event) {
        var status = repository.findById(event.paymentProcessId().value())
            .orElseThrow();
        status.setCurrentState(PaymentProcessState.CREDIT_ACCEPTED);
        status.setLastUpdated(LocalDateTime.now());
        repository.save(status);
    }

    @EventHandler
    public void on(BankTransferRequestedEvent event) {
        var status = repository.findById(event.paymentProcessId().value())
            .orElseThrow();
        status.setCurrentState(PaymentProcessState.BANK_TRANSFER_REQUESTED);
        status.setLastUpdated(LocalDateTime.now());
        repository.save(status);
    }

    @EventHandler
    public void on(PaymentProcessCompletedEvent event) {
        var status = repository.findById(event.paymentProcessId().value())
            .orElseThrow();
        status.setCurrentState(PaymentProcessState.COMPLETED);
        status.setLastUpdated(LocalDateTime.now());
        repository.save(status);
    }
}
