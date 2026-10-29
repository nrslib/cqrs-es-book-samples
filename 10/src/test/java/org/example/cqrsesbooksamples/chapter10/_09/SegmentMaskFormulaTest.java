// 検証補助: 10.5
package org.example.cqrsesbooksamples.chapter10._09;

import org.axonframework.eventhandling.Segment;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;


// 書籍リスト10.5の式 (sequenceIdentifier.hashCode() & segmentMask) == segmentId が
// AxonのSegment#matchesによる振り分け判定と一致することを確かめる検証テスト
class SegmentMaskFormulaTest {

    @Test
    void 識別子はハッシュ値とマスクの論理積でセグメントへ振り分けられる() {
        Segment[] segments = Segment.computeSegments(0, 1, 2, 3);
        for (int i = 0; i < 1000; i++) {
            String sequenceIdentifier = "aggregate-" + i;
            for (Segment segment : segments) {
                boolean formula =
                    (sequenceIdentifier.hashCode() & segment.getMask()) == segment.getSegmentId();
                assertEquals(formula, segment.matches(sequenceIdentifier),
                    "segment=" + segment.getSegmentId() + " id=" + sequenceIdentifier);
            }
        }
    }

    @Test
    void ひとつの識別子はちょうどひとつのセグメントにマッチする() {
        Segment[] segments = Segment.computeSegments(0, 1, 2, 3);
        for (int i = 0; i < 1000; i++) {
            String sequenceIdentifier = "aggregate-" + i;
            long matched = Arrays.stream(segments)
                .filter(s -> s.matches(sequenceIdentifier))
                .count();
            assertEquals(1, matched, "id=" + sequenceIdentifier);
        }
    }
}
