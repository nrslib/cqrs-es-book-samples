package org.example.cqrsesbooksamples.chapter02._76;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.StreamingEventProcessor;
import org.springframework.stereotype.Service;

// 削除済み（旧 2.77）
@Service
public class ProjectionManagementService {
    private final EventProcessingConfiguration config;

    public ProjectionManagementService(EventProcessingConfiguration config) {
        this.config = config;
    }

    public void resetUserProjection() {
        // user-projectionグループだけをリセット
        // user-notificationグループは影響を受けない
        config.eventProcessorByProcessingGroup("user-projection", StreamingEventProcessor.class)
            .ifPresent(processor -> {
                processor.shutDown();
                processor.resetTokens();
                processor.start();
            });
    }
}
