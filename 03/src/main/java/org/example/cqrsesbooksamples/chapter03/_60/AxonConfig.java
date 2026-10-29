// 紙面照合: 3.44
package org.example.cqrsesbooksamples.chapter03._60;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventMessageHandler;
import org.axonframework.eventhandling.EventProcessingException;
import org.axonframework.eventhandling.ListenerInvocationErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AxonConfig {
    @Autowired
    public void configureEventProcessing(EventProcessingConfigurer configurer) {
        configurer.registerListenerInvocationErrorHandler(
            "user-projection",
            conf -> new RetryingErrorHandler(3, 1000) // 3回リトライ、初回の待ち時間1秒
        );
    }

    private static class RetryingErrorHandler implements ListenerInvocationErrorHandler {
        private final int maxRetries;
        private final long retryInterval;

        public RetryingErrorHandler(int maxRetries, long retryInterval) {
            this.maxRetries = maxRetries;
            this.retryInterval = retryInterval;
        }

        @Override
        public void onError(Exception exception, EventMessage<?> event, EventMessageHandler handler) {
            int attempt = 0;
            while (attempt < maxRetries) {
                try {
                    Thread.sleep(retryInterval * (1L << attempt)); // 指数バックオフ
                    handler.handle(event);
                    return; // 成功したら終了
                } catch (Exception e) {
                    attempt++;
                    if (attempt >= maxRetries) {
                        throw new EventProcessingException(
                            "イベント処理が" + maxRetries + "回失敗しました", e
                        );
                    }
                }
            }
        }
    }
}
