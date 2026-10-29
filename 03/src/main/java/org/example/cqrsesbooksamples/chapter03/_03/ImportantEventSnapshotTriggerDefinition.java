// 紙面照合: 3.2
package org.example.cqrsesbooksamples.chapter03._03;

import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.SnapshotTrigger;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;


public class ImportantEventSnapshotTriggerDefinition implements SnapshotTriggerDefinition {
    private final Snapshotter snapshotter;

    public ImportantEventSnapshotTriggerDefinition(Snapshotter snapshotter) {
        this.snapshotter = snapshotter;
    }

    @Override
    public SnapshotTrigger prepareTrigger(Class<?> aggregateType) {
        return new SnapshotTrigger() {
            @Override
            public void eventHandled(EventMessage<?> message) {
                // 節目となるイベントのときだけスナップショットを予約する
                if ((message.getPayload() instanceof PriceChangedEvent
                        || message.getPayload() instanceof StatusChangedEvent)
                        && message instanceof DomainEventMessage<?> domainEvent) {
                    snapshotter.scheduleSnapshot(
                        aggregateType, domainEvent.getAggregateIdentifier());
                }
            }

            @Override
            public void initializationFinished() {
            }
        };
    }
}
