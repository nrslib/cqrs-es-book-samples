package org.example.cqrsesbooksamples.chapter04;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.PropagatingErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;

// 本書の標準に合わせ、ストリーミング型の既定をPooledStreamingEventProcessorにする
@AutoConfiguration
public class PooledProcessorDefaultConfig {
    @Autowired
    public void configure(EventProcessingConfigurer configurer) {
        configurer.usingPooledStreamingEventProcessors();
        // ハンドラ例外は握りつぶさず伝播させ、同じ位置から再試行する（第3章で示したエラーハンドラ登録と同じ方針）
        configurer.registerDefaultListenerInvocationErrorHandler(conf -> PropagatingErrorHandler.instance());
    }
}
