// 紙面照合: 3.18
package org.example.cqrsesbooksamples.chapter03._51;


public record SegmentOperationResult(
    String instanceId,
    int segmentId,
    OperationStatus status,
    String message
) {}
