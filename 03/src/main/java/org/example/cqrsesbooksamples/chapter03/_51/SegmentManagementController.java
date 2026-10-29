// 紙面照合: 3.17, 3.18, 3.20
package org.example.cqrsesbooksamples.chapter03._51;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.EventTrackerStatus;
import org.axonframework.eventhandling.StreamingEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/admin/segments")
public class SegmentManagementController {
    private static final Logger log = LoggerFactory.getLogger(SegmentManagementController.class);

    private final EventProcessingConfiguration config;
    private final String instanceId;

    public SegmentManagementController(EventProcessingConfiguration config) {
        this.config = config;
        // インスタンスIDはPod名（HOSTNAME）、なければホスト名、それもなければUUID
        this.instanceId = Optional.ofNullable(System.getenv("HOSTNAME"))
            .orElseGet(() -> {
                try {
                    return InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    return UUID.randomUUID().toString();
                }
            });
    }

    @PostMapping("/{processingGroup}/split/{segmentId}")
    public CompletableFuture<ResponseEntity<SegmentOperationResult>> splitSegment(
            @PathVariable String processingGroup,
            @PathVariable int segmentId) {

        return config.eventProcessor(processingGroup, StreamingEventProcessor.class)
            .map(processor -> {
                // このインスタンスが該当セグメントを所有しているか確認
                EventTrackerStatus status = processor.processingStatus().get(segmentId);

                if (status == null) {
                    // このインスタンスは該当セグメントを所有していない
                    return CompletableFuture.completedFuture(
                        ResponseEntity.ok(new SegmentOperationResult(
                            instanceId,
                            segmentId,
                            OperationStatus.NOT_OWNED,
                            "This instance does not own segment " + segmentId
                        ))
                    );
                }

                // このインスタンスが所有しているので、分割を実行
                return processor.splitSegment(segmentId)
                    .thenApply(success -> {
                        if (success) {
                            log.info("Instance {} successfully split segment {}",
                                instanceId, segmentId);
                            return ResponseEntity.ok(new SegmentOperationResult(
                                instanceId,
                                segmentId,
                                OperationStatus.SUCCESS,
                                "Segment split successfully"
                            ));
                        } else {
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new SegmentOperationResult(
                                    instanceId,
                                    segmentId,
                                    OperationStatus.FAILED,
                                    "Failed to split segment"
                                ));
                        }
                    });
            })
            .orElse(CompletableFuture.completedFuture(
                ResponseEntity.notFound().build()
            ));
    }

    @PostMapping("/{processingGroup}/merge/{segmentId}")
    public CompletableFuture<ResponseEntity<SegmentOperationResult>> mergeSegment(
            @PathVariable String processingGroup,
            @PathVariable int segmentId) {
        // split と同様の実装
        return config.eventProcessor(processingGroup, StreamingEventProcessor.class)
            .map(processor -> {
                EventTrackerStatus status = processor.processingStatus().get(segmentId);

                if (status == null) {
                    return CompletableFuture.completedFuture(
                        ResponseEntity.ok(new SegmentOperationResult(
                            instanceId,
                            segmentId,
                            OperationStatus.NOT_OWNED,
                            "This instance does not own segment " + segmentId
                        ))
                    );
                }

                return processor.mergeSegment(segmentId)
                    .thenApply(success -> {
                        if (success) {
                            return ResponseEntity.ok(new SegmentOperationResult(
                                instanceId,
                                segmentId,
                                OperationStatus.SUCCESS,
                                "Segment merged successfully"
                            ));
                        } else {
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new SegmentOperationResult(
                                    instanceId,
                                    segmentId,
                                    OperationStatus.FAILED,
                                    "Failed to merge segment"
                                ));
                        }
                    });
            })
            .orElse(CompletableFuture.completedFuture(
                ResponseEntity.notFound().build()
            ));
    }


    // このインスタンスが所有するセグメント情報を返す
    @GetMapping("/{processingGroup}/ownership")
    public ResponseEntity<InstanceSegmentOwnership> getSegmentOwnership(
            @PathVariable String processingGroup) {
        return config.eventProcessor(processingGroup, StreamingEventProcessor.class)
            .map(processor -> {
                List<SegmentInfo> ownedSegments = new ArrayList<>();

                processor.processingStatus().forEach((segmentId, status) -> {
                    ownedSegments.add(new SegmentInfo(
                        segmentId,
                        status.getCurrentPosition().orElse(0),
                        status.isCaughtUp(),
                        status.isErrorState()
                    ));
                });

                return ResponseEntity.ok(new InstanceSegmentOwnership(
                    instanceId,
                    processingGroup,
                    ownedSegments
                ));
            })
            .orElse(ResponseEntity.notFound().build());
    }


    // ステップ1: 全インスタンスへブロードキャストしてプロセッサを停止する
    @PostMapping("/{processingGroup}/stop")
    public ResponseEntity<Void> stopProcessor(@PathVariable String processingGroup) {
        return config.eventProcessor(processingGroup, StreamingEventProcessor.class)
            .map(processor -> {
                processor.shutDown();
                return ResponseEntity.ok().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // ステップ2: 全インスタンスの停止を確認してから、どれか1台だけで実行する
    @PostMapping("/{processingGroup}/reset")
    public ResponseEntity<Void> resetProcessor(@PathVariable String processingGroup) {
        return config.eventProcessor(processingGroup, StreamingEventProcessor.class)
            .map(processor -> {
                processor.resetTokens();  // 全セグメントのトークンをまとめてリセット
                return ResponseEntity.ok().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // ステップ3: 全インスタンスへブロードキャストして再開する
    @PostMapping("/{processingGroup}/start")
    public ResponseEntity<Void> startProcessor(@PathVariable String processingGroup) {
        return config.eventProcessor(processingGroup, StreamingEventProcessor.class)
            .map(processor -> {
                processor.start();
                return ResponseEntity.ok().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
