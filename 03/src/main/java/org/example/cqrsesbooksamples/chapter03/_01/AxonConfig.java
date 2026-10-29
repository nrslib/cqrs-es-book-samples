// 紙面照合: 3.1
package org.example.cqrsesbooksamples.chapter03._01;

import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AxonConfig {
    @Bean
    public SnapshotTriggerDefinition productSnapshotTrigger(
        Snapshotter snapshotter
    ) {
        return new EventCountSnapshotTriggerDefinition(
            snapshotter,
            100  // 100イベントごとにスナップショット作成
        );
    }
}
