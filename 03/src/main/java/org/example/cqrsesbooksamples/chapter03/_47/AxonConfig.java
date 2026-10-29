// 紙面照合: 3.14
package org.example.cqrsesbooksamples.chapter03._47;

import java.util.concurrent.Executors;

import org.axonframework.common.AxonThreadFactory;
import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.beans.factory.annotation.Autowired;


@org.springframework.context.annotation.Configuration
public class AxonConfig {
    @Autowired
    public void configureEventProcessing(EventProcessingConfigurer configurer) {
        configurer.registerPooledStreamingEventProcessor(
            "user-projection",
            Configuration::eventStore,
            (config, builder) -> builder
                // 同時に動く処理の上限となるワーカープールのスレッド数
                .workerExecutor(name -> Executors.newScheduledThreadPool(
                    4, new AxonThreadFactory("worker-" + name)))
                // 1インスタンスがクレームするセグメント数の上限
                .maxClaimedSegments(8)
        );
    }
}
