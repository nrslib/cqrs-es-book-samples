package org.example.cqrsesbooksamples.chapter03._38;

import static org.assertj.core.api.Assertions.assertThat;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter03_38;DB_CLOSE_DELAY=-1")
class PooledProcessorRegistrationTest {
    @Autowired
    private EventProcessingConfiguration config;

    @Test
    void 登録した処理グループがPooledStreamingEventProcessorとして動作する() {
        assertThat(config.eventProcessor("event-handler-group", PooledStreamingEventProcessor.class)).isPresent();
        assertThat(config.eventProcessor("user-projection", PooledStreamingEventProcessor.class)).isPresent();
    }
}
