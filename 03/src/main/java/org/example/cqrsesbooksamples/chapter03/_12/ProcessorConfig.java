// 紙面照合: 3.8
package org.example.cqrsesbooksamples.chapter03._12;

import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.messaging.StreamableMessageSource;
import org.springframework.beans.factory.annotation.Autowired;


@org.springframework.context.annotation.Configuration
public class ProcessorConfig {
    @Autowired
    public void configure(EventProcessingConfigurer configurer) {
        configurer.registerPooledStreamingEventProcessor(
            "user-projection",
            Configuration::eventStore,
            (config, builder) -> builder
                .initialSegmentCount(4)
                .batchSize(100)
                .initialToken(StreamableMessageSource::createHeadToken)
        );
    }
}
