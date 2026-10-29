// 紙面照合: 3.2
package org.example.cqrsesbooksamples.chapter03._03;

import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


// Snapshotter本体はイベントストアのBeanがあればSpring Bootの自動構成が登録する
@Configuration
public class AxonConfig {
    @Bean
    public SnapshotTriggerDefinition customSnapshotTrigger(Snapshotter snapshotter) {
        return new ImportantEventSnapshotTriggerDefinition(snapshotter);
    }
}
