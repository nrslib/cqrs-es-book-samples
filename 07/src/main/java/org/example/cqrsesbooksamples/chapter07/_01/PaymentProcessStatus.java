// 紙面照合: 7.2
package org.example.cqrsesbooksamples.chapter07._01;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "payment_process_status")
public class PaymentProcessStatus {
    @Id
    private String processId;

    @Enumerated(EnumType.STRING)
    private PaymentProcessState currentState;

    private LocalDateTime lastUpdated;

    protected PaymentProcessStatus() {}

    public PaymentProcessStatus(String processId) {
        this.processId = processId;
    }

    public String getProcessId() { return processId; }
    public PaymentProcessState getCurrentState() { return currentState; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }

    public void setProcessId(String processId) { this.processId = processId; }
    public void setCurrentState(PaymentProcessState currentState) { this.currentState = currentState; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
