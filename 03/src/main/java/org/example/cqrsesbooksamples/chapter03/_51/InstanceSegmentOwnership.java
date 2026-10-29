// 検証補助: 3.17
package org.example.cqrsesbooksamples.chapter03._51;

import java.util.List;


public record InstanceSegmentOwnership(
    String instanceId,
    String processingGroup,
    List<SegmentInfo> ownedSegments
) {}
