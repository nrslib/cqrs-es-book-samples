// 紙面照合: 9.19

package org.example.cqrsesbooksamples.chapter09._22;

import org.example.cqrsesbooksamples.chapter09._01.domain.slot.CreateReservationSlotCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class SubSlotCreationService {

    private final CommandGateway commandGateway;

    public SubSlotCreationService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    public void createSlotWithSubSlots(String slotId, LocalDate date,
            LocalTime start, LocalTime end, int capacity) {
        int subSlotCount = 4;
        if (capacity <= 0 || capacity % subSlotCount != 0) {
            throw new IllegalArgumentException("定員には正の4の倍数を指定してください");
        }
        int perSubSlot = capacity / subSlotCount;
        for (int i = 0; i < subSlotCount; i++) {
            commandGateway.sendAndWait(
                new CreateReservationSlotCommand(
                    slotId + "-sub-" + i, date, start, end, perSubSlot));
        }
    }
}
