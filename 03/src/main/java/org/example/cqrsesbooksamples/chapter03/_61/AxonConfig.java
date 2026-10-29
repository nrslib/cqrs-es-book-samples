// 紙面照合: 3.45
package org.example.cqrsesbooksamples.chapter03._61;

import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.deadletter.jpa.JpaSequencedDeadLetterQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AxonConfig {
    @Autowired
    public void configureEventProcessing(EventProcessingConfigurer configurer) {
        configurer.registerDeadLetterQueue(
            "user-projection",
            conf -> JpaSequencedDeadLetterQueue.builder()
                .processingGroup("user-projection")
                .entityManagerProvider(conf.getComponent(EntityManagerProvider.class))
                .transactionManager(conf.getComponent(TransactionManager.class))
                .serializer(conf.eventSerializer())
                .build()
        );
    }
}
