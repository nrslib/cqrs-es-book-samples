// 紙面照合: 3.33
package org.example.cqrsesbooksamples.chapter03._21;

import org.axonframework.serialization.upcasting.event.EventUpcasterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class UpcasterConfiguration {

    @Bean
    public EventUpcasterChain eventUpcasterChain() {
        return new EventUpcasterChain(
            new UserRegisteredEventV1ToV1_1Upcaster(),
            new UserRegisteredEventV1_1ToV2Upcaster()
        );
    }
}
