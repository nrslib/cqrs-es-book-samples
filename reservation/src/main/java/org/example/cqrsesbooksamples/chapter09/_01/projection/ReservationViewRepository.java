package org.example.cqrsesbooksamples.chapter09._01.projection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationViewRepository extends JpaRepository<ReservationView, String> {
    @Modifying
    @Query("UPDATE ReservationView r SET r.status = :status WHERE r.reservationId = :reservationId")
    void updateStatus(String reservationId, String status);
}
