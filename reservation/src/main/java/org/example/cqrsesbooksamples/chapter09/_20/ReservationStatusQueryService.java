package org.example.cqrsesbooksamples.chapter09._20;

import org.example.cqrsesbooksamples.chapter09._01.projection.ReservationViewRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReservationStatusQueryService {

    private final ReservationViewRepository repository;

    public ReservationStatusQueryService(ReservationViewRepository repository) {
        this.repository = repository;
    }

    public Optional<ReservationStatusResponse> findById(String reservationId) {
        return repository.findById(reservationId)
            .map(ReservationStatusResponse::from);
    }
}
