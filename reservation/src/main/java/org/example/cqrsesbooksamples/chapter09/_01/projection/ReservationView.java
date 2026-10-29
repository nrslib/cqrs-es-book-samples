package org.example.cqrsesbooksamples.chapter09._01.projection;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservation_view")
public class ReservationView {
    @Id
    private String reservationId;
    private String slotId;
    private String participantId;
    private String status;

    protected ReservationView() {}

    public ReservationView(String reservationId, String slotId, String participantId, String status) {
        this.reservationId = reservationId;
        this.slotId = slotId;
        this.participantId = participantId;
        this.status = status;
    }

    public String getReservationId() { return reservationId; }
    public String getSlotId() { return slotId; }
    public String getParticipantId() { return participantId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
