// 紙面照合: 9.17

package org.example.cqrsesbooksamples.chapter09._20;

import org.example.cqrsesbooksamples.chapter09._01.domain.reservation.RequestReservationCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationRequestQueue requestQueue;
    private final ReservationStatusQueryService statusQueryService;

    public ReservationController(
            ReservationRequestQueue requestQueue,
            ReservationStatusQueryService statusQueryService) {
        this.requestQueue = requestQueue;
        this.statusQueryService = statusQueryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ReservationAcceptedResponse requestReservation(
            @RequestBody ReservationRequest request) {
        var reservationId = UUID.randomUUID().toString();

        // コマンドは直接送らず、受付キューへ積む
        requestQueue.enqueue(
            new RequestReservationCommand(reservationId, request.participantId(), request.slotId()));

        // 受け付けた事実だけを返す
        return new ReservationAcceptedResponse(reservationId);
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationStatusResponse> getReservation(
            @PathVariable String reservationId) {
        return statusQueryService.findById(reservationId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
