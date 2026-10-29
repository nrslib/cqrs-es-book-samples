package org.example.cqrsesbooksamples.chapter07._01;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentProcessStatusRepository extends JpaRepository<PaymentProcessStatus, String> {
}
