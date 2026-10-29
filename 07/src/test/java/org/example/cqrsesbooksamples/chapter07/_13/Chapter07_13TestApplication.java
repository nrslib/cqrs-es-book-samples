package org.example.cqrsesbooksamples.chapter07._13;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = {
        org.example.cqrsesbooksamples.chapter07._06.PaymentProcessAggregateAdapter.class,
        org.example.cqrsesbooksamples.chapter07._13.PaymentProcessEventStoreQuery.class
})
public class Chapter07_13TestApplication {
}
