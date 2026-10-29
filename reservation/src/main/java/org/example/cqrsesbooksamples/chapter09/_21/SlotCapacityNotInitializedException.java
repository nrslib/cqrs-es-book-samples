package org.example.cqrsesbooksamples.chapter09._21;

public class SlotCapacityNotInitializedException extends IllegalStateException {
    public SlotCapacityNotInitializedException(String slotId) {
        super("予約枠の残席カウンタが初期化されていません: " + slotId);
    }
}
