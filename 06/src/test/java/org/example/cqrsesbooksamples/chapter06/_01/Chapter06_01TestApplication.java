package org.example.cqrsesbooksamples.chapter06._01;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Chapter06_01TestApplication {
    @Bean
    public StubCreditApi creditApi() {
        return new StubCreditApi();
    }

    @Bean
    public StubBankTransferApi bankTransferApi() {
        return new StubBankTransferApi();
    }
}
