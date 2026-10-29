// 紙面照合: 3.46
package org.example.cqrsesbooksamples.chapter03._62;

import java.util.Map;

import org.axonframework.config.EventProcessingConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/admin/dead-letters")
public class DeadLetterQueueController {
    private final EventProcessingConfiguration config;

    public DeadLetterQueueController(EventProcessingConfiguration config) {
        this.config = config;
    }

    @PostMapping("/{processingGroup}/process")
    public ResponseEntity<String> processDeadLetters(@PathVariable String processingGroup) {
        return config.sequencedDeadLetterProcessor(processingGroup)
            .map(processor -> {
                // 先頭のシーケンスをひとつ再処理する（成功すればDLQから取り除かれる）
                boolean processed = processor.processAny();
                return ResponseEntity.ok(processed
                    ? "Dead letter sequence processed for " + processingGroup
                    : "No dead letters to process for " + processingGroup);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{processingGroup}/process-all")
    public ResponseEntity<Map<String, Object>> processAllDeadLetters(
            @PathVariable String processingGroup) {
        return config.sequencedDeadLetterProcessor(processingGroup)
            .map(processor -> {
                int processed = 0;
                // 再処理できるシーケンスがなくなるまで繰り返す
                while (processor.processAny()) {
                    processed++;
                }

                Map<String, Object> result = Map.of("processedSequences", processed);
                return ResponseEntity.ok(result);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{processingGroup}/clear")
    public ResponseEntity<String> clearDeadLetters(@PathVariable String processingGroup) {
        return config.deadLetterQueue(processingGroup)
            .map(queue -> {
                queue.clear();
                return ResponseEntity.ok("Dead letter queue cleared for " + processingGroup);
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
