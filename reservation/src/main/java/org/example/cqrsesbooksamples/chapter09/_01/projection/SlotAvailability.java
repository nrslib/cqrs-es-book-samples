package org.example.cqrsesbooksamples.chapter09._01.projection;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "slot_availability")
public class SlotAvailability {
    @Id
    private String slotId;
    private LocalDate slotDate;
    private int capacity;
    private int remaining;

    protected SlotAvailability() {}

    public SlotAvailability(String slotId, LocalDate slotDate, int capacity, int remaining) {
        this.slotId = slotId;
        this.slotDate = slotDate;
        this.capacity = capacity;
        this.remaining = remaining;
    }

    public String getSlotId() { return slotId; }
    public LocalDate getSlotDate() { return slotDate; }
    public int getCapacity() { return capacity; }
    public int getRemaining() { return remaining; }
}
