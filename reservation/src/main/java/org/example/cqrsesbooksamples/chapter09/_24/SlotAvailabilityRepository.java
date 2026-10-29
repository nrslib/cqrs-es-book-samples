// 紙面照合: 9.21

package org.example.cqrsesbooksamples.chapter09._24;

// 確保数と定員はリードモデルから取得する
public interface SlotAvailabilityRepository {
    long countClaimed(String slotId);
    long getCapacity(String slotId);
}
