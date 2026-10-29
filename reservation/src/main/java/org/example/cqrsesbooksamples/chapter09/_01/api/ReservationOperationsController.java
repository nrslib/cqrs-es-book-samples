package org.example.cqrsesbooksamples.chapter09._01.api;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.ApproveReservationCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.CancelReservationCommand;
import org.example.cqrsesbooksamples.chapter09._01.domain.slot.CreateReservationSlotCommand;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@RestController
public class ReservationOperationsController {
    private final CommandGateway commandGateway;

    public ReservationOperationsController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @PostMapping("/reservation-slots")
    @ResponseStatus(HttpStatus.CREATED)
    public SlotCreatedResponse createSlot(@RequestBody SlotRequest request) {
        var slotId = UUID.randomUUID().toString();
        commandGateway.sendAndWait(new CreateReservationSlotCommand(slotId, request.slotDate(),
            request.startTime(), request.endTime(), request.capacity()));
        return new SlotCreatedResponse(slotId);
    }

    @PostMapping("/reservations/{reservationId}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approve(@PathVariable String reservationId, @RequestBody ApprovalRequest request) {
        commandGateway.sendAndWait(new ApproveReservationCommand(reservationId, request.approvedBy()));
    }

    @PostMapping("/reservations/{reservationId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable String reservationId, @RequestBody CancellationRequest request) {
        commandGateway.sendAndWait(new CancelReservationCommand(reservationId, request.reason()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void invalidInput() {}

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public void invalidTransition() {}

    @ExceptionHandler(AggregateNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound() {}

    public record SlotRequest(LocalDate slotDate, LocalTime startTime, LocalTime endTime, int capacity) {
        public SlotRequest {
            if (slotDate == null || startTime == null || endTime == null) {
                throw new IllegalArgumentException("予約枠の日付と開始・終了時刻は必須です");
            }
        }
    }

    public record SlotCreatedResponse(String slotId) {}
    public record ApprovalRequest(String approvedBy) {}
    public record CancellationRequest(String reason) {}
}
