// 紙面照合: 3.10
package org.example.cqrsesbooksamples.chapter03._41;

import org.axonframework.config.EventProcessingConfiguration;
import java.util.Collections;

import org.axonframework.eventhandling.GapAwareTrackingToken;
import org.axonframework.eventhandling.StreamingEventProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/admin")
public class ProjectionAdminController {
    private final EventProcessingConfiguration config;

    public ProjectionAdminController(EventProcessingConfiguration config) {
        this.config = config;
    }

    @PostMapping("/reset/{processingGroup}/to-position")
    public ResponseEntity<String> resetProcessorToPosition(
            @PathVariable String processingGroup,
            @RequestParam long position) {
        return config.eventProcessor(processingGroup, StreamingEventProcessor.class)
            .map(processor -> {
                processor.shutDown();
                processor.resetTokens(streamableMessageSource ->
                    GapAwareTrackingToken.newInstance(position, Collections.emptyList()));
                processor.start();
                return ResponseEntity.ok("Processor " + processingGroup +
                    " has been reset to position " + position);
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
