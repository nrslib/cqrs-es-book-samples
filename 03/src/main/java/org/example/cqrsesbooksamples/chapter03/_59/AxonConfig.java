// 紙面照合: 3.43
package org.example.cqrsesbooksamples.chapter03._59;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.PropagatingErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AxonConfig {
    @Autowired
    public void configureEventProcessing(EventProcessingConfigurer configurer) {
        configurer.registerListenerInvocationErrorHandler(
            "user-projection",
            conf -> PropagatingErrorHandler.instance()
        );

        configurer.registerErrorHandler(
            "user-projection",
            conf -> PropagatingErrorHandler.instance()
        );
    }
}
