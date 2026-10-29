// 紙面照合: 9.14

package org.example.cqrsesbooksamples.chapter09._01.config;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.PropagatingErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;

@AutoConfiguration
public class PooledProcessorDefaultConfig {
    @Autowired
    public void configure(EventProcessingConfigurer configurer) {
        configurer.usingPooledStreamingEventProcessors();
        configurer.registerDefaultListenerInvocationErrorHandler(conf -> PropagatingErrorHandler.instance());
    }
}
