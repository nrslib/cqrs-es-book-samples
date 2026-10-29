package org.example.cqrsesbooksamples.chapter03._68;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.PropagatingErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Chapter03_68TestApplication {
    // 失敗を握りつぶさず伝播させ、トランザクションごとロールバックさせる
    @Autowired
    public void configure(EventProcessingConfigurer configurer) {
        configurer.registerListenerInvocationErrorHandler(
            "retry-projection", conf -> PropagatingErrorHandler.instance());
    }
}
