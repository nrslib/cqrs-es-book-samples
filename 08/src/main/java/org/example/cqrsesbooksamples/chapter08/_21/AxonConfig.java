package org.example.cqrsesbooksamples.chapter08._21;

import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.deadletter.jpa.JpaSequencedDeadLetterQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * order-projection グループにデッドレターキューを設定する。
 */
@Configuration
public class AxonConfig {
    @Autowired
    public void configureEventProcessing(EventProcessingConfigurer configurer) {
        configurer.registerDeadLetterQueue(
            "order-projection",
            conf -> JpaSequencedDeadLetterQueue.builder()
                .processingGroup("order-projection")
                .entityManagerProvider(conf.getComponent(EntityManagerProvider.class))
                .transactionManager(conf.getComponent(TransactionManager.class))
                .serializer(conf.eventSerializer())
                .build()
        );
    }
}
