package org.example.cqrsesbooksamples.chapter10._09;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.StreamingEventProcessor;
import org.springframework.stereotype.Component;

@Component
public class ProcessorSegmentController {
    private final EventProcessingConfiguration eventProcessingConfiguration;

    public ProcessorSegmentController(EventProcessingConfiguration eventProcessingConfiguration) {
        this.eventProcessingConfiguration = eventProcessingConfiguration;
    }

    public void splitSegment(String processorName, int segmentId) {
        StreamingEventProcessor processor = eventProcessingConfiguration
            .eventProcessor(processorName, StreamingEventProcessor.class)
            .orElseThrow(() -> new IllegalArgumentException("Processor not found"));

        boolean succeeded = processor.splitSegment(segmentId).join();
        if (!succeeded) {
            throw new IllegalStateException(
                "Failed to split segment " + segmentId + " of " + processorName);
        }
    }

    public void mergeSegment(String processorName, int segmentId) {
        StreamingEventProcessor processor = eventProcessingConfiguration
            .eventProcessor(processorName, StreamingEventProcessor.class)
            .orElseThrow(() -> new IllegalArgumentException("Processor not found"));

        boolean succeeded = processor.mergeSegment(segmentId).join();
        if (!succeeded) {
            throw new IllegalStateException(
                "Failed to merge segment " + segmentId + " of " + processorName);
        }
    }
}
