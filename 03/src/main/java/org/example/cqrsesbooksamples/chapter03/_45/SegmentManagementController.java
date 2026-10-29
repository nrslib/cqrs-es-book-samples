// 紙面照合: 3.12
package org.example.cqrsesbooksamples.chapter03._45;

import java.util.concurrent.CompletableFuture;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.StreamingEventProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/admin/segments")
public class SegmentManagementController {
    private final EventProcessingConfiguration config;

    public SegmentManagementController(EventProcessingConfiguration config) {
        this.config = config;
    }

    @PostMapping("/{processingGroup}/split/{segmentId}")
    public CompletableFuture<ResponseEntity<String>> splitSegment(
            @PathVariable String processingGroup,
            @PathVariable int segmentId) {

        return config.eventProcessor(processingGroup, StreamingEventProcessor.class)
            .map(processor -> processor.splitSegment(segmentId)
                .thenApply(success ->
                    success
                        ? ResponseEntity.ok("Segment " + segmentId + " has been split")
                        : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to split segment " + segmentId)
                ))
            .orElse(CompletableFuture.completedFuture(
                ResponseEntity.notFound().build()
            ));
    }

    @PostMapping("/{processingGroup}/merge/{segmentId}")
    public CompletableFuture<ResponseEntity<String>> mergeSegment(
            @PathVariable String processingGroup,
            @PathVariable int segmentId) {

        return config.eventProcessor(processingGroup, StreamingEventProcessor.class)
            .map(processor -> processor.mergeSegment(segmentId)
                .thenApply(success ->
                    success
                        ? ResponseEntity.ok("Segment " + segmentId + " has been merged")
                        : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to merge segment " + segmentId)
                ))
            .orElse(CompletableFuture.completedFuture(
                ResponseEntity.notFound().build()
            ));
    }

    @PostMapping("/{processingGroup}/release/{segmentId}")
    public ResponseEntity<String> releaseSegment(
            @PathVariable String processingGroup,
            @PathVariable int segmentId) {
        return config.eventProcessor(processingGroup, StreamingEventProcessor.class)
            .map(processor -> {
                processor.releaseSegment(segmentId);
                return ResponseEntity.ok("Segment " + segmentId + " has been released");
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
