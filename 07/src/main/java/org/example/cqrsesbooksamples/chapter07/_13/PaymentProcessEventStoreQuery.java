// 紙面照合: 7.11
package org.example.cqrsesbooksamples.chapter07._13;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.stereotype.Component;


@Component
public class PaymentProcessEventStoreQuery {
    private final EventStore eventStore;

    public PaymentProcessEventStoreQuery(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    // readEventsは集約識別子を取り、単一集約のストリームを読む
    public List<DomainEventMessage<?>> getEventsForAggregate(String aggregateId) {
        return eventStore.readEvents(aggregateId)
            .asStream()
            .collect(Collectors.toList());
    }

    public List<EventMessage<?>> getEventsWithMetadata(String processId) {
        // メタデータに特定のprocessIdを含むイベントを、ストリームの先頭から探索する
        var events = new ArrayList<EventMessage<?>>();
        try (var stream = eventStore.openStream(null)) {
            while (stream.hasNextAvailable()) {
                var event = stream.nextAvailable();
                if (processId.equals(String.valueOf(event.getMetaData().get("processId")))) {
                    events.add(event);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return events;
    }
}
