// 紙面照合: 3.9
package org.example.cqrsesbooksamples.chapter03._14;

import java.time.Instant;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.StreamingEventProcessor;
import org.springframework.stereotype.Service;

@Service
public class RebuildRestartService {
    private final EventProcessingConfiguration eventProcessingConfiguration;

    public RebuildRestartService(EventProcessingConfiguration eventProcessingConfiguration) {
        this.eventProcessingConfiguration = eventProcessingConfiguration;
    }


    public void restartFromTimestamp(Instant timestamp) {
        StreamingEventProcessor processor = eventProcessingConfiguration
            .eventProcessor("user-projection", StreamingEventProcessor.class)
            .orElseThrow();

        processor.shutDown();
        processor.resetTokens(streamableMessageSource ->
            streamableMessageSource.createTokenAt(timestamp));
        processor.start();
    }
}
