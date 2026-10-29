package org.example.cqrsesbooksamples.chapter09._01;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Import;
import org.example.cqrsesbooksamples.chapter09._20.ReservationController;
import org.example.cqrsesbooksamples.chapter09._20.ReservationRequestQueue;
import org.example.cqrsesbooksamples.chapter09._20.ReservationStatusQueryService;

@SpringBootApplication
@Import({ReservationController.class, ReservationRequestQueue.class, ReservationStatusQueryService.class})
@EnableScheduling
public class ReservationApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReservationApplication.class, args);
    }
}
