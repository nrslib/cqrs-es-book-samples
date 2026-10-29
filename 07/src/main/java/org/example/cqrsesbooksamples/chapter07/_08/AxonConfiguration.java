// 紙面照合: 7.6
package org.example.cqrsesbooksamples.chapter07._08;

import java.util.List;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.messaging.correlation.MessageOriginProvider;
import org.axonframework.messaging.correlation.MultiCorrelationDataProvider;
import org.axonframework.messaging.correlation.SimpleCorrelationDataProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AxonConfiguration {
    @Bean
    public CorrelationDataProvider processIdCorrelationDataProvider() {
        return new MultiCorrelationDataProvider<CommandMessage<?>>(
                List.of(
                        new SimpleCorrelationDataProvider("processId"),
                        new MessageOriginProvider()
                )
        );
    }
}
