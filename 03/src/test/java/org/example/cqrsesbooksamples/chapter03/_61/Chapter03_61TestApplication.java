package org.example.cqrsesbooksamples.chapter03._61;

import org.example.cqrsesbooksamples.chapter03._62.DeadLetterQueueController;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Chapter03_61TestApplication {
    @Bean
    public DeadLetterQueueController deadLetterQueueController(
            org.axonframework.config.EventProcessingConfiguration config) {
        return new DeadLetterQueueController(config);
    }
}
