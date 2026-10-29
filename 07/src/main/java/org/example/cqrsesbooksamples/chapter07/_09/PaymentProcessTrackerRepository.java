package org.example.cqrsesbooksamples.chapter07._09;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentProcessTrackerRepository extends JpaRepository<PaymentProcessTracker, String> {
}
