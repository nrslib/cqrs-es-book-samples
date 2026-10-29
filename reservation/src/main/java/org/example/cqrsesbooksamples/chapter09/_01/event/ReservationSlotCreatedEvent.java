// 削除済み（旧 9.7）
package org.example.cqrsesbooksamples.chapter09._01.event;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationSlotCreatedEvent(
    String slotId,
    LocalDate slotDate,
    LocalTime startTime,
    LocalTime endTime,
    int capacity
) implements ReservationSlotEvent {}
