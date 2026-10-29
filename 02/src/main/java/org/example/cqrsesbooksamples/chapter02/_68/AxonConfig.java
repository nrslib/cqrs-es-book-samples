// 紙面照合: 2.54
package org.example.cqrsesbooksamples.chapter02._68;

import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.beans.factory.annotation.Autowired;


@org.springframework.context.annotation.Configuration
public class AxonConfig {
    @Autowired
    public void configure(EventProcessingConfigurer configurer) {
        configurer.registerPooledStreamingEventProcessor(
            "user-projection",
            Configuration::eventStore,
            (config, builder) -> builder.initialSegmentCount(4)
        );
    }
}
