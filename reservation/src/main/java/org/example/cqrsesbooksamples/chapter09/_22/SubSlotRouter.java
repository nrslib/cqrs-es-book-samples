// 紙面照合: 9.20

package org.example.cqrsesbooksamples.chapter09._22;

public class SubSlotRouter {
    private static final int SUB_SLOT_COUNT = 4;

    public String selectSubSlot(String slotId, String reservationId) {
        int index = Math.abs(reservationId.hashCode() % SUB_SLOT_COUNT);
        return slotId + "-sub-" + index;
    }
}
