// 紙面照合: 2.58
package org.example.cqrsesbooksamples.chapter02._73;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.StreamingEventProcessor;
import org.springframework.stereotype.Service;


@Service
public class ProjectionManagementService {
    private final EventProcessingConfiguration config;

    public ProjectionManagementService(EventProcessingConfiguration config) {
        this.config = config;
    }

    public void resetUserProjection() {
        config.eventProcessorByProcessingGroup("user-projection", StreamingEventProcessor.class)
            .ifPresent(processor -> {
                processor.shutDown();           // プロセッサを停止
                processor.resetTokens();        // トークンをリセット
                processor.start();              // プロセッサを再起動
            });
    }
}
