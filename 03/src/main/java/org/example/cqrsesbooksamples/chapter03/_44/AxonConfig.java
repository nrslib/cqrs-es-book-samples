package org.example.cqrsesbooksamples.chapter03._44;

import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.beans.factory.annotation.Autowired;

// 削除済み（旧 3.19）
@org.springframework.context.annotation.Configuration
public class AxonConfig {
    @Autowired
    public void configureEventProcessing(EventProcessingConfigurer configurer) {
        configurer.registerPooledStreamingEventProcessor(
            "user-projection",
            Configuration::eventStore,
            (config, builder) -> builder.initialSegmentCount(4)
        );
    }
}
