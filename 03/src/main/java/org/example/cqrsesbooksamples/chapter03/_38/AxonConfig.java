package org.example.cqrsesbooksamples.chapter03._38;

import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.beans.factory.annotation.Autowired;

// 削除済み（旧 3.54）
@org.springframework.context.annotation.Configuration
public class AxonConfig {
    @Autowired
    public void configure(EventProcessingConfigurer configurer) {
        configurer.registerPooledStreamingEventProcessor(
            "event-handler-group",
            Configuration::eventStore
        );

        configurer.registerPooledStreamingEventProcessor(
            "user-projection",
            Configuration::eventStore
        );
    }
}
