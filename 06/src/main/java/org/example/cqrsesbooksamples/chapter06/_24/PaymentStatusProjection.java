package org.example.cqrsesbooksamples.chapter06._24;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.example.cqrsesbooksamples.chapter06._01.CreditRejectedEvent;
import org.example.cqrsesbooksamples.chapter06._01.PaymentProcessCompletedEvent;
import org.example.cqrsesbooksamples.chapter06._01.PaymentProcessAcceptedEvent;
import org.springframework.stereotype.Component;

/**
 * 同期返却用に決済の進行状況を保持し、subscriptionQuery へ更新を流すプロジェクション。
 */
@Component
public class PaymentStatusProjection {
    private final QueryUpdateEmitter updateEmitter;
    private final Map<String, PaymentStatus> statuses = new ConcurrentHashMap<>();

    public PaymentStatusProjection(QueryUpdateEmitter updateEmitter) {
        this.updateEmitter = updateEmitter;
    }

    @EventHandler
    public void on(PaymentProcessAcceptedEvent event) {
        update(event.paymentProcessId().value(), PaymentStatus.PENDING);
    }

    @EventHandler
    public void on(PaymentProcessCompletedEvent event) {
        update(event.paymentProcessId().value(), PaymentStatus.COMPLETED);
    }

    @EventHandler
    public void on(CreditRejectedEvent event) {
        update(event.paymentProcessId().value(), PaymentStatus.FAILED);
    }

    private void update(String processId, PaymentStatus status) {
        statuses.put(processId, status);
        updateEmitter.emit(PaymentStatusQuery.class,
            query -> query.processId().toString().equals(processId),
            status);
    }

    @QueryHandler
    public PaymentStatus handle(PaymentStatusQuery query) {
        return statuses.getOrDefault(query.processId().toString(), PaymentStatus.PENDING);
    }
}
