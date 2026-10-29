// 検証補助: 3.17
package org.example.cqrsesbooksamples.chapter03._51;


public record SegmentInfo(
    int segmentId,
    long position,
    boolean caughtUp,
    boolean error
) {}
