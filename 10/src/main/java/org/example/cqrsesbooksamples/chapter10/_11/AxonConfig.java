// 紙面照合: 10.6
package org.example.cqrsesbooksamples.chapter10._11;

import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.beans.factory.annotation.Autowired;


@org.springframework.context.annotation.Configuration
public class AxonConfig {
    @Autowired
    public void configure(EventProcessingConfigurer configurer) {
        configurer.registerPooledStreamingEventProcessor(
            "document-view",
            Configuration::eventStore,
            (config, builder) -> builder
                .batchSize(100)          // 1トランザクションで処理するイベントのバッチサイズ
                .initialSegmentCount(1)  // 例を単一セグメントで動かすための指定
        );
    }
}
