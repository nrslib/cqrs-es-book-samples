package org.example.cqrsesbooksamples.chapter03._13;

import java.util.concurrent.CompletableFuture;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.StreamingEventProcessor;
import org.springframework.stereotype.Service;

// 削除済み（旧 3.13）
@Service
public class ProcessorScalingService {
    private final EventProcessingConfiguration config;

    public ProcessorScalingService(EventProcessingConfiguration config) {
        this.config = config;
    }

    // プロセッサのセグメント数を動的に調整
    public CompletableFuture<Boolean> splitSegment(String processorName, int segmentId) {
        StreamingEventProcessor processor = config
            .eventProcessor(processorName, StreamingEventProcessor.class)
            .orElseThrow();

        return processor.splitSegment(segmentId);  // セグメントをふたつに分割
    }
}
