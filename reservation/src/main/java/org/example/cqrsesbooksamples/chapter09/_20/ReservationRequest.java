// 検証補助: 9.17

package org.example.cqrsesbooksamples.chapter09._20;

public record ReservationRequest(String participantId, String slotId) {
    public ReservationRequest {
        if (participantId == null || participantId.isBlank()) {
            throw new IllegalArgumentException("参加者IDは必須です");
        }
        if (slotId == null || slotId.isBlank()) {
            throw new IllegalArgumentException("予約枠IDは必須です");
        }
    }
}
