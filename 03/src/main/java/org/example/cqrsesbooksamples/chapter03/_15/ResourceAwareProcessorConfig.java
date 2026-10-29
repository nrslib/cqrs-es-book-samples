package org.example.cqrsesbooksamples.chapter03._15;

import java.util.concurrent.Executors;

import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.beans.factory.annotation.Autowired;

// 削除済み（旧 3.15）
@org.springframework.context.annotation.Configuration
public class ResourceAwareProcessorConfig {
    @Autowired
    public void configure(EventProcessingConfigurer configurer) {
        configurer.registerPooledStreamingEventProcessor(
            "user-projection",
            Configuration::eventStore,
            (config, builder) -> builder
                .batchSize(100)
                // 優先度を下げた4スレッドのワーカープールを設定
                .workerExecutor(name -> Executors.newScheduledThreadPool(4, r -> {
                    Thread t = new Thread(r, name + "-worker");
                    t.setPriority(Thread.MIN_PRIORITY);  // 優先度を下げる
                    return t;
                }))
        );
    }
}
